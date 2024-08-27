package com.hazzabro124.marionetta.blocks

import com.hazzabro124.marionetta.MarionettaProperties
import com.hazzabro124.marionetta.ship.MarionettaShips
import com.hazzabro124.marionetta.util.DirectionalShape
import com.hazzabro124.marionetta.util.PlayerReference
import com.hazzabro124.marionetta.util.RotShapes
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.chat.TextComponent
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.DirectionalBlock
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.material.Material
import net.minecraft.world.level.storage.loot.LootContext
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape
import org.valkyrienskies.core.api.ships.ServerShip
import org.valkyrienskies.mod.common.getShipManagingPos
import org.valkyrienskies.mod.common.getShipObjectManagingPos
import org.valkyrienskies.mod.common.util.toJOML
import org.valkyrienskies.mod.common.util.toJOMLD
import java.util.*

class ProxyBlock(
    private val mult: () -> Double,
):DirectionalBlock(
    Properties.of(Material.STONE)
        .sound(SoundType.STONE)
        .strength(1.0f,2.0f)
) {
    private val BASE = RotShapes.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0)
    private val BOX = RotShapes.box(3.0, 5.0, 3.0, 13.0, 15.0, 13.0)

    private val Proxy_SHAPE = DirectionalShape.up(RotShapes.or(BASE, BOX))

    private var boundplayer: PlayerReference? = null

    init {
        registerDefaultState(
            defaultBlockState()
                .setValue(FACING, Direction.NORTH)
                .setValue(BlockStateProperties.POWER, 0)
                .setValue(MarionettaProperties.CONTROLLER, MarionettaShips.ControllerTypeEnum.controller0)
        )
    }

    override fun getRenderShape(state: BlockState): RenderShape {
        return RenderShape.MODEL
    }

    override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape {
        return Proxy_SHAPE[state.getValue(BlockStateProperties.FACING)]
    }

    override fun use(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hand: InteractionHand,
        hit: BlockHitResult
    ): InteractionResult {
        val server = player.server
        val blockState: BlockState;
        if (server != null) {
            val overworld = server.getLevel(Level.OVERWORLD)
            val entity = overworld?.getEntity(player.uuid)

            if (entity is ServerPlayer) {
                boundplayer = PlayerReference.from(entity)

                blockState = state.cycle(MarionettaProperties.CONTROLLER)
                level.setBlock(pos, blockState, 3)
                level.updateNeighborsAt(pos, this)

                player.sendMessage(TextComponent("bound to ${player.name.contents}'s ${state.getValue(MarionettaProperties.CONTROLLER)}"), player.uuid)
                player.sendMessage(TextComponent("${(player.lookAngle).toJOML()}"), player.uuid)
            } else {
                // TODO Handle the exceptions
            }
        } else {
            // TODO Handle the other exceptions
        }

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

        if (signal > 0) {
            enableProxy(level, pos, state)
        }
    }

    override fun onRemove(state: BlockState, level: Level, pos: BlockPos, newState: BlockState, isMoving: Boolean) {
        if (level !is ServerLevel) return

        disableProxy(level, pos)

        super.onRemove(state, level, pos, newState, isMoving)
    }


    private fun getShipControl(level: Level, pos: BlockPos) =
        ((level.getShipObjectManagingPos(pos) ?: level.getShipManagingPos(pos)) as?
                ServerShip)?.let { MarionettaShips.getOrCreate(it) }

    private fun enableProxy(level: ServerLevel, pos: BlockPos, state: BlockState) {
        getShipControl(level, pos)?.let {
            it.stopProxy(pos)
            it.addProxy(
                pos,
                state.getValue(FACING).normal.toJOMLD()
                    .mul(state.getValue(BlockStateProperties.POWER).toDouble()
                    * mult()),
                boundplayer as PlayerReference,
                state.getValue(MarionettaProperties.CONTROLLER)
            )
        }
    }

    private fun disableProxy(level: ServerLevel, pos: BlockPos) {
        getShipControl(level, pos)?.stopProxy(pos)
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

        disableProxy(level, pos)
        level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.POWER, signal))
    }

    override fun getStateForPlacement(ctx: BlockPlaceContext): BlockState {
        var dir = ctx.clickedFace

        if (ctx.player != null && ctx.player!!.isShiftKeyDown)
            dir = dir.opposite

        return defaultBlockState()
            .setValue(BlockStateProperties.FACING, dir)

    }
}