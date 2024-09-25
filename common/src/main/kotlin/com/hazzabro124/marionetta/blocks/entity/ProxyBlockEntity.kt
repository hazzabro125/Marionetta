package com.hazzabro124.marionetta.blocks.entity

import com.hazzabro124.marionetta.blocks.MarionettaBlockEntities
import com.hazzabro124.marionetta.MarionettaProperties
import com.hazzabro124.marionetta.VRPlugin
import com.hazzabro124.marionetta.blocks.custom.ProxyAnchorBlock
import com.hazzabro124.marionetta.ship.MarionettaShips
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import org.joml.Quaterniond
import org.joml.Vector3d
import org.valkyrienskies.mod.common.getShipObjectManagingPos
import org.valkyrienskies.mod.common.util.toJOML
import java.lang.Math.toRadians
import java.util.UUID

class ProxyBlockEntity(pos: BlockPos, state: BlockState) : BlockEntity(MarionettaBlockEntities.PROXY.get(), pos, state) {
    var boundPlayer: UUID? = null
    var anchorPos: BlockPos? = null

    fun bindPlayer(player: Player) {
        if (VRPlugin.vrAPI?.playerInVR(player) != true) return
        boundPlayer = player.uuid
        this.setChanged()
    }

    fun linkAnchor(pos: BlockPos) {
        anchorPos = pos
        this.setChanged()
    }

    fun getAndValidateAnchor(level: ServerLevel): BlockPos? {
        if (anchorPos == null) return null
        if (level.isLoaded(anchorPos!!)) return null

        val state = level.getBlockState(anchorPos!!)
        if (state.block !is ProxyAnchorBlock) {
            anchorPos = null
            this.setChanged()
            return anchorPos
        }
        if (state.getValue(BlockStateProperties.POWER) <= 0) return null

        return anchorPos!!
    }

    override fun saveAdditional(tag: CompoundTag) {
        if (boundPlayer != null)
            tag.putUUID("marionetta\$player", boundPlayer!!)
        if (anchorPos != null)
            tag.putLong("marionetta\$anchorPos", anchorPos!!.asLong())

        super.saveAdditional(tag)
    }

    override fun load(tag: CompoundTag) {
        super.load(tag)

        if (tag.contains("marionetta\$player"))
            boundPlayer = tag.getUUID("marionetta\$player")
        if (tag.contains("marionetta\$anchorPos"))
            anchorPos = BlockPos.of(tag.getLong("marionetta\$anchorPos"))
    }

    companion object {
        fun tick(level: Level, pos: BlockPos, state: BlockState, proxy: BlockEntity) {
            if (level.isClientSide || proxy !is ProxyBlockEntity) return
            val level = level as ServerLevel

            // If not on a Ship, return, no need in trying to apply forces ot something that doesn't exist
            val ship = level.getShipObjectManagingPos(pos) ?: return

            // If signal <= 0, return
            if (state.getValue(BlockStateProperties.POWER) <= 0) return

            // If no player with UUID exists or UUID is null, return
            val player = proxy.boundPlayer?.let { level.getPlayerByUUID(it) } ?: return
            // If player not in VR or VRAPI is null, return
            val vrPlayer = VRPlugin.vrAPI?.getVRPlayer(player) ?: return

            val controllerType = state.getValue(MarionettaProperties.CONTROLLER)
            val controller = when(controllerType) {
                MarionettaShips.ControllerTypeEnum.controller0 -> vrPlayer.controller0
                else -> vrPlayer.controller1
            }

            val quat = Quaterniond().rotateYXZ(
                toRadians(-vrPlayer.hmd.yaw.toDouble()),
                toRadians(vrPlayer.hmd.pitch.toDouble()),
                toRadians(0.0),
            )

            val scale = 4.0
            var xOffset = -0.25
            val yOffset = 0.25
            val zOffset = 0.0

            if (controllerType == MarionettaShips.ControllerTypeEnum.controller1)
                xOffset *= -1

            val idealPos: Vector3d =
                controller.position().toJOML()
                    .sub(vrPlayer.hmd.position().toJOML())
                    .add(quat.transform(Vector3d(xOffset, yOffset, zOffset)))
                    .mul(scale)
                    .add(vrPlayer.hmd.position().toJOML())

            val forcesApplier = MarionettaShips.getOrCreate(ship)
            forcesApplier.addProxy(pos, idealPos, controller, proxy.getAndValidateAnchor(level))
        }
    }
}