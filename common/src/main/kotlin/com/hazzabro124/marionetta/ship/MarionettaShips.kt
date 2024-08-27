package com.hazzabro124.marionetta.ship

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonIgnore
import com.hazzabro124.marionetta.GravitronForceInducerData
import com.hazzabro124.marionetta.MarionettaMod
import com.hazzabro124.marionetta.TickScheduler
import com.hazzabro124.marionetta.VRPlugin
import com.hazzabro124.marionetta.util.PlayerReference
import com.hazzabro124.marionetta.util.extension.toDimensionKey
import com.hazzabro124.marionetta.util.extension.toDouble
import com.mojang.math.Quaternion
import net.blf02.vrapi.api.data.IVRData
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.TextComponent
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.StringRepresentable
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3
import org.joml.Quaterniond
import org.joml.Vector3d
import org.joml.Vector3dc
import org.joml.Vector3i
import org.valkyrienskies.core.api.ships.*
import org.valkyrienskies.core.apigame.world.properties.DimensionId
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl
import org.valkyrienskies.mod.common.util.toJOML
import java.lang.Math.toRadians
import java.util.concurrent.CopyOnWriteArrayList

@JsonAutoDetect(
    fieldVisibility = JsonAutoDetect.Visibility.ANY,
    getterVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE,
    setterVisibility = JsonAutoDetect.Visibility.NONE
)
class MarionettaShips: ShipForcesInducer {
    var xkp: Double = 0.0
    var level: DimensionId = "minecraft:overworld"

    @JsonIgnore
    var globalLevel: ServerLevel? = null

    data class ProxyData(
        val pos: Vector3i,
        val force: Vector3d,
        val boundPlayer: PlayerReference,
        var controllerType: ControllerTypeEnum
    )

     enum class ControllerTypeEnum(val value:String): StringRepresentable {
         controller0("controller0"),
         controller1("controller1");

         override fun getSerializedName(): String {
             return value
         }

         companion object {
             fun fromString(name: String): ControllerTypeEnum {
                 return values().firstOrNull { it.value == name } ?: controller0
             }
         }
    }

    var data: GravitronForceInducerData? = null

    val proxies = CopyOnWriteArrayList<ProxyData>()

    @JsonIgnore
    private var ticker: TickScheduler.Ticking? = null

    fun IVRData.relativepositon(boundPlayer: Player): Vec3 {
        return this.position().subtract(boundPlayer.position())
    }

    override fun applyForces(physShip: PhysShip) {
        physShip as PhysShipImpl

        if (ticker == null) {
            ticker = TickScheduler.serverTickPerm { server ->
                val lvl = server.getLevel(level.toDimensionKey())
                globalLevel = lvl
                    ?: return@serverTickPerm
            }
        }

        val vel = physShip.poseVel.vel

        proxies.forEach { data ->
            val (pos, force, boundPlayer, controllerType) = data
            var boundplayer2: ServerPlayer? = null

            // Check if globalLevel is not null before attempting to resolve
            if (globalLevel != null) {
                boundplayer2 = boundPlayer.resolve(globalLevel!!)
            }

            // Now boundplayer2 can be safely used, and it won't be null
            if (boundplayer2 != null) {
                if (VRPlugin.vrAPI!!.getVRPlayer(boundplayer2) != null) {
                    var controller = if (controllerType == ControllerTypeEnum.controller1) VRPlugin.vrAPI!!.getVRPlayer(boundplayer2).controller1
                    else VRPlugin.vrAPI!!.getVRPlayer(boundplayer2).controller0

                    val quat4 = Quaterniond().rotateYXZ(
                        toRadians(-VRPlugin.vrAPI!!.getVRPlayer(boundplayer2).hmd.yaw.toDouble()),
                        toRadians(VRPlugin.vrAPI!!.getVRPlayer(boundplayer2).hmd.pitch.toDouble()),
                        toRadians(0.0),
                    )

                    var scale = 4.0
                    var xOffset = -0.25
                    var yOffset = 0.25
                    var zOffset = 0.0

                    if (controllerType == ControllerTypeEnum.controller1) {
                        xOffset *= -1
                    }

                    var idealPos: Vector3d =
                        VRPlugin.vrAPI!!.getVRPlayer(boundplayer2).controller0.position().toJOML()
                            .sub(VRPlugin.vrAPI!!.getVRPlayer(boundplayer2).hmd.position().toJOML())
                            .add(quat4.transform(Vector3d(xOffset, yOffset, zOffset)))
                            .mul(scale)
                            .add(VRPlugin.vrAPI!!.getVRPlayer(boundplayer2).hmd.position().toJOML())

                    val pConst = 160.0
                    val dConst = 20.0

                    val localGrabPos: Vector3d? =
                        physShip.transform.shipToWorld.transformPosition(pos.toDouble().add(0.5, 0.5, 0.5), Vector3d())
                    val idealPosDif: Vector3dc = idealPos.sub(localGrabPos, Vector3d())

                    val posDif: Vector3d = idealPosDif.mul(pConst, Vector3d())
                    val mass = physShip.inertia.shipMass

                    // Integrate
                    posDif.sub(physShip.poseVel.vel.mul(dConst, Vector3d()))

                    val force3 = posDif.mul(mass, Vector3d())
                    boundplayer2.sendMessage(TextComponent("Applied Force: $force3"), boundplayer2.uuid)
                    physShip.applyInvariantForce(force3)

                    val pConstR = 160.0
                    val dConstR = 20.0

                    val quat2 =
                        Quaterniond().rotateYXZ(
                            toRadians(-VRPlugin.vrAPI!!.getVRPlayer(boundplayer2).controller0.yaw.toDouble()),
                            toRadians(-VRPlugin.vrAPI!!.getVRPlayer(boundplayer2).controller0.pitch.toDouble()),
                            toRadians(VRPlugin.vrAPI!!.getVRPlayer(boundplayer2).controller0.roll.toDouble()),
                        )

                    if (quat2 != null) {
                        val rotDif =
                            quat2.mul(physShip.transform.shipToWorldRotation.invert(Quaterniond()), Quaterniond())
                                .normalize().invert()

                        val rotDifVector = Vector3d(rotDif.x() * 2.0, rotDif.y() * 2.0, rotDif.z() * 2.0).mul(pConstR)

                        if (rotDif.w() < 0) {
                            rotDifVector.mul(-1.0)
                        }

                        rotDifVector.mul(-1.0)

                        // Integrate
                        rotDifVector.sub(physShip.poseVel.omega.mul(dConstR, Vector3d()))

                        val torque2 = physShip.transform.shipToWorldRotation.transform(
                            physShip.inertia.momentOfInertiaTensor.transform(
                                physShip.transform.shipToWorldRotation.transformInverse(
                                    rotDifVector,
                                    Vector3d()
                                )
                            )
                        )

                        boundplayer2.sendMessage(TextComponent("Applied Torque: $torque2"), boundplayer2.uuid)
                        physShip.applyInvariantForce(torque2)
                    }

                    boundplayer2.sendMessage(TextComponent("Bound to ${boundplayer2.name.contents}"), boundplayer2.uuid)
                    boundplayer2.sendMessage(TextComponent("Rotation =)"), boundplayer2.uuid)
                }
            }
        }
    }

    fun addProxy(
        pos: BlockPos,
        force: Vector3d,
        boundPlayer: PlayerReference,
        controllerType: ControllerTypeEnum
    ) {
        proxies += ProxyData(pos.toJOML(), force, boundPlayer, controllerType)
    }

    fun addProxies(
        list: Iterable<ProxyData>
    ){
        list.forEach { (pos, force, boundPlayer, controllerType) ->
            proxies += ProxyData(pos, force, boundPlayer, controllerType)
        }
    }

    fun stopProxy(
        pos: BlockPos
    ){
        proxies.removeIf { pos.toJOML() == it.pos }
    }

    companion object {
        fun getOrCreate(ship: ServerShip, level: DimensionId) =
            ship.getAttachment<MarionettaShips>()
                ?: MarionettaShips().also {
                    it.level = level
                    ship.saveAttachment(it)
                }

        fun getOrCreate(ship: ServerShip): MarionettaShips =
            getOrCreate(ship, ship.chunkClaimDimension)
    }
}