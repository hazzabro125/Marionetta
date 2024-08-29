package com.hazzabro124.marionetta.ship

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonIgnore
import com.hazzabro124.marionetta.TickScheduler
import com.hazzabro124.marionetta.VRPlugin
import com.hazzabro124.marionetta.blocks.ProxyAnchor.Companion.anchors
import com.hazzabro124.marionetta.util.PlayerReference
import com.hazzabro124.marionetta.util.extension.toDimensionKey
import com.hazzabro124.marionetta.util.extension.toDouble
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.TextComponent
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.StringRepresentable
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

    /**
     * Data class for proxy block.
     * @property pos            Position of the block in the level ([Vector3i]).
     * @property boundPlayer    The player bound to the proxy ([PlayerReference]).
     * @property controllerType The type of VR controller bound to the proxy ([ControllerTypeEnum]).
     */
    data class ProxyData(
        val pos: Vector3i,
        val boundPlayer: PlayerReference,
        var controllerType: ControllerTypeEnum,
        var anchorReference: BlockPos?
    )

    data class AnchorData(
        val pos: Vector3i,
    )

    /**
     * Enum specifying VR controller type.
     */
    enum class ControllerTypeEnum(val value:String): StringRepresentable {
        /**
         * Typically corresponds to right controller
         */
        controller0("controller0"),
        /**
         * Typically corresponds to left controller
         */
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

    val proxies = CopyOnWriteArrayList<ProxyData>()

    @JsonIgnore
    private var ticker: TickScheduler.Ticking? = null

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
            val (pos, boundPlayer, controllerType,anchorReference) = data
            var boundplayer2: ServerPlayer? = null
            var linkedAnchor = getAnchor(anchorReference!!.toJOML())


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

                    val scale = 4.0
                    var xOffset = -0.25
                    val yOffset = 0.25
                    val zOffset = 0.0

                    if (controllerType == ControllerTypeEnum.controller1) {
                        xOffset *= -1
                    }

                    val idealPos: Vector3d =
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

    /**
     * Add proxy to be processed.
     * @param pos               the position of the proxy ([BlockPos]).
     * @param boundPlayer       the player bound to the proxy ([PlayerReference]).
     * @param controllerType    the type of controller bound to the proxy ([ControllerTypeEnum]).
     * @see addProxies
     * @see stopProxy
     */
    fun addProxy(
        pos: BlockPos,
        boundPlayer: PlayerReference,
        controllerType: ControllerTypeEnum,
        anchorReference: BlockPos?
    ) {
        proxies += ProxyData(pos.toJOML(), boundPlayer, controllerType, anchorReference)
    }

    /**
     * Add multiple proxies to be processed.
     * @param list the iterable list of proxy data to be added ([Iterable]<[ProxyData]>).
     * @see addProxy
     * @see stopProxy
     */
    fun addProxies(
        list: Iterable<ProxyData>
    ){
        list.forEach { (pos, boundPlayer, controllerType, anchorReference) ->
            proxies += ProxyData(pos, boundPlayer, controllerType, anchorReference)
        }
    }

    /**
     * Ceases the processing of a proxy.
     * @param pos the position of the proxy to stop ([BlockPos]).
     * @see addProxy
     * @see addProxies
     */
    fun stopProxy(
        pos: BlockPos
    ) {
        proxies.removeIf { pos.toJOML() == it.pos }
    }
        fun getAnchor(pos: Vector3i): MarionettaShips.AnchorData? {
            return anchors.find{it == MarionettaShips.AnchorData(pos)}
        }

    companion object {
        /**
         * Gets or creates a VS ship attachment
         * @param ship the ship to apply to ([ServerShip]).
         * @param level the dimension of the ship ([DimensionId]).
         */
        fun getOrCreate(ship: ServerShip, level: DimensionId) =
            ship.getAttachment<MarionettaShips>()
                ?: MarionettaShips().also {
                    it.level = level
                    ship.saveAttachment(it)
                }

        /**
         * Gets or creates a VS ship attachment
         * @param ship the ship to apply to ([ServerShip]).
         */
        fun getOrCreate(ship: ServerShip): MarionettaShips =
            getOrCreate(ship, ship.chunkClaimDimension)
    }
}