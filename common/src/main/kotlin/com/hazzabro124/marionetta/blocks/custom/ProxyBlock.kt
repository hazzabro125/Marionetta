package com.hazzabro124.marionetta.blocks.custom

import com.hazzabro124.marionetta.blocks.MarionettaBlockEntities
import com.hazzabro124.marionetta.MarionettaProperties
import com.hazzabro124.marionetta.blocks.entity.ProxyBlockEntity
import com.hazzabro124.marionetta.ship.MarionettaShips
import com.hazzabro124.marionetta.util.DirectionalShape
import com.hazzabro124.marionetta.util.RotShapes
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.chat.TextComponent
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.DirectionalBlock
import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.material.Material
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape

class ProxyBlock: DirectionalBlock(
    Properties.of(Material.STONE)
        .sound(SoundType.STONE)
        .strength(1.0f,2.0f)
), EntityBlock {
    private val BASE = RotShapes.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0)
    private val BOX = RotShapes.box(3.0, 5.0, 3.0, 13.0, 15.0, 13.0)

    private val PROXY_SHAPE = DirectionalShape.up(RotShapes.or(BASE, BOX))

    init {
        registerDefaultState(
            defaultBlockState()
                .setValue(FACING, Direction.NORTH)
                .setValue(BlockStateProperties.POWER, 0)
                .setValue(MarionettaProperties.CONTROLLER, MarionettaShips.ControllerTypeEnum.controller0)
        )
    }

    @Deprecated("Deprecated in Java")
    override fun getRenderShape(state: BlockState): RenderShape {
        return RenderShape.MODEL
    }

    @Deprecated("Deprecated in Java")
    override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape {
        return PROXY_SHAPE[state.getValue(BlockStateProperties.FACING)]
    }

    @Deprecated("Deprecated in Java")
    override fun use(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hand: InteractionHand,
        hit: BlockHitResult
    ): InteractionResult {
        if (level.isClientSide)
            return InteractionResult.sidedSuccess(true)

        if (!player.getItemInHand(hand).isEmpty) return InteractionResult.PASS
        
        val be = level.getBlockEntity(pos)
        if (be !is ProxyBlockEntity) return InteractionResult.FAIL
        
        if (be.boundPlayer != player.uuid) {
            player.sendMessage(TextComponent("Bound ${player.name.string} to Proxy!"), player.uuid)
            be.bindPlayer(player)
        }

        val newState = state.cycle(MarionettaProperties.CONTROLLER)
        player.sendMessage(TextComponent("Set Proxy's Controller to ${newState.getValue(MarionettaProperties.CONTROLLER).value}!"), player.uuid)
        level.setBlock(pos, newState, 3)
        level.updateNeighborsAt(pos, this)
        
        return InteractionResult.CONSUME
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(FACING)
        builder.add(BlockStateProperties.POWER)
        builder.add(MarionettaProperties.CONTROLLER)
        super.createBlockStateDefinition(builder)
    }

    override fun onPlace(state: BlockState, level: Level, pos: BlockPos, oldState: BlockState, isMoving: Boolean) {
        super.onPlace(state, level, pos, oldState, isMoving)

        if (level !is ServerLevel) return

        val signal = level.getBestNeighborSignal(pos)
        if (state.getValue(BlockStateProperties.POWER) != signal) {
            level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.POWER, signal))
            return
        }
    }

    override fun neighborChanged(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        block: Block,
        fromPos: BlockPos,
        isMoving: Boolean
    ) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving)

        if (level !is ServerLevel) return

        val signal = level.getBestNeighborSignal(pos)
        val prev = state.getValue(BlockStateProperties.POWER)

        if (signal == prev) return
        
        level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.POWER, signal))
    }

    override fun getStateForPlacement(ctx: BlockPlaceContext): BlockState {
        var dir = ctx.clickedFace

        if (ctx.player != null && ctx.player!!.isShiftKeyDown)
            dir = dir.opposite

        return defaultBlockState()
            .setValue(BlockStateProperties.FACING, dir)

    }

    override fun newBlockEntity(pos: BlockPos, state: BlockState) = ProxyBlockEntity(pos, state)

    override fun <T : BlockEntity> getTicker(
        level: Level,
        state: BlockState,
        blockEntityType: BlockEntityType<T>
    ): BlockEntityTicker<T>? {
        return if (blockEntityType == MarionettaBlockEntities.PROXY.get()) BlockEntityTicker<T>(ProxyBlockEntity::tick) else null
    }
}