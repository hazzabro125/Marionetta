package com.hazzabro124.marionetta.blocks.entity

import com.hazzabro124.marionetta.blocks.MarionettaBlockEntities
import com.hazzabro124.marionetta.MarionettaProperties
import com.hazzabro124.marionetta.VRPlugin
import com.hazzabro124.marionetta.blocks.custom.ProxyAnchorBlock
import com.hazzabro124.marionetta.ship.MarionettaShips
import com.hazzabro124.marionetta.util.extension.toBlock
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.TextComponent
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
import org.valkyrienskies.mod.common.util.toJOMLD
import java.lang.Math.toRadians
import java.util.UUID

class ProxyBlockEntity(pos: BlockPos, state: BlockState) : BlockEntity(MarionettaBlockEntities.PROXY.get(), pos, state) {
    var boundPlayer: UUID? = null
    var anchorPos: BlockPos? = null

    fun bindPlayer(player: Player) {
        if (VRPlugin.vrAPI?.playerInVR(player) != true) {
            player.sendMessage(TextComponent("Cannnot bind to a non-VR player!"), player.uuid)
            return
        }
        boundPlayer = player.uuid
        this.setChanged()
    }

    fun linkAnchor(pos: BlockPos) {
        anchorPos = pos
        this.setChanged()
    }

    fun getAndValidateAnchor(level: ServerLevel): BlockPos? {
        if (anchorPos == null) return null

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
            if (level !is ServerLevel || proxy !is ProxyBlockEntity) return

            // If not on a Ship, return, no need in trying to apply forces ot something that doesn't exist
            val ship = level.getShipObjectManagingPos(pos) ?: return

            // If signal <= 0, return
            if (state.getValue(BlockStateProperties.POWER) <= 0) return

            // If no player with UUID exists or UUID is null, return
            val player = proxy.boundPlayer?.let { level.getPlayerByUUID(it) } ?: return

            // If player not in VR or VRAPI is null, return
            val vrPlayer = VRPlugin.vrAPI?.getVRPlayer(player) ?: return

            var anchorDirection: Vector3d? = null
            val anchorPos = proxy.getAndValidateAnchor(level)?.let { anchor ->
                val anchorShip = level.getShipObjectManagingPos(anchor)
                if (anchorShip == null) {
                    anchorDirection = level.getBlockState(anchor).getValue(BlockStateProperties.FACING).normal.toJOMLD()
                    anchor
                } else {
                    anchorDirection = anchorShip.transform.shipToWorldRotation.transform(
                        level.getBlockState(anchor).getValue(BlockStateProperties.FACING).normal.toJOMLD(), Vector3d())
                    anchorShip.transform.shipToWorld.transformPosition(anchor.toJOMLD()).toBlock()
                }
            }

            val controllerType = state.getValue(MarionettaProperties.CONTROLLER)
            val settings = when (controllerType) {
                MarionettaShips.ControllerTypeEnum.controller1 -> MarionettaShips.ProxySettings(xOffset = -0.25 * -1.0)
                else -> MarionettaShips.ProxySettings()
            }

            val forcesApplier = MarionettaShips.getOrCreate(ship)
            forcesApplier.addProxy(pos, vrPlayer, controllerType, anchorPos, anchorDirection, settings)
        }
    }
}