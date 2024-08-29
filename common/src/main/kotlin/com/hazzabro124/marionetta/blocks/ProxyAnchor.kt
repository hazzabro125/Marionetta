package com.hazzabro124.marionetta.blocks

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.DirectionalBlock
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.material.Material
import org.valkyrienskies.mod.common.util.toJOML
import com.hazzabro124.marionetta.ship.MarionettaShips
import java.util.concurrent.CopyOnWriteArrayList


class ProxyAnchor(
) : DirectionalBlock(
    Properties.of(Material.STONE)
        .sound(SoundType.STONE)
        .strength(1.0f, 2.0f)
) {

    init {
        registerDefaultState(
            defaultBlockState()
                .setValue(FACING, Direction.NORTH)
                .setValue(BlockStateProperties.POWER, 0)
        )
    }

    override fun getRenderShape(blockState: BlockState): RenderShape {
        return RenderShape.MODEL
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(FACING)
        builder.add(BlockStateProperties.POWER)
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
            enableAnchor(level, pos, state)
        }
    }

    override fun onRemove(state: BlockState, level: Level, pos: BlockPos, newState: BlockState, isMoving: Boolean) {
        if (level !is ServerLevel) return

        super.onRemove(state, level, pos, newState, isMoving)
    }

    private fun enableAnchor(level: ServerLevel, pos: BlockPos, state: BlockState) {
            stopAnchor(pos)
            addAnchor(pos)
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
    companion object {
        val anchors = CopyOnWriteArrayList<MarionettaShips.AnchorData>()

        fun addAnchor(
            pos: BlockPos,
        ) {
            anchors += MarionettaShips.AnchorData(pos.toJOML())
        }
        fun stopAnchor(
            pos: BlockPos
        ) {
            anchors.removeIf { pos.toJOML() == it.pos }
        }

    }
}