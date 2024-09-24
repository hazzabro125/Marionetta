package com.hazzabro124.marionetta.ship

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonIgnore
import com.hazzabro124.marionetta.TickScheduler
import com.hazzabro124.marionetta.VRPlugin
import com.hazzabro124.marionetta.blocks.ProxyAnchor.Companion.anchors
import com.hazzabro124.marionetta.util.PlayerReference
import com.hazzabro124.marionetta.util.extension.toDimensionKey
import com.hazzabro124.marionetta.util.extension.toDouble
import net.blf02.vrapi.api.data.IVRPlayer
import net.blf02.vrapi.data.VRPlayer
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
import org.valkyrienskies.core.util.pollUntilEmpty
import org.valkyrienskies.mod.common.util.toJOML
import java.lang.Math.toRadians
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CopyOnWriteArrayList

@JsonAutoDetect(
    fieldVisibility = JsonAutoDetect.Visibility.ANY,
    getterVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE,
    setterVisibility = JsonAutoDetect.Visibility.NONE
)
class MarionettaShips: ShipForcesInducer {
    var xkp: Double = 0.0

    /**
     * Data class for proxy block updates.
     * @property pos            Position of the proxy block on a Ship ([Vector3i])
     * @property idealPos       The ideal Ship position ([Vector3d])
     * @property vrPlayer       The VRPlayer bound to the proxy ([IVRPlayer])
     */
    data class ProxyUpdateData(val pos: Vector3i, val idealPos: Vector3d, val vrPlayer: IVRPlayer)

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

    val proxyUpdates = ConcurrentLinkedQueue<ProxyUpdateData>()

    override fun applyForces(physShip: PhysShip) {
        physShip as PhysShipImpl

        val vel = physShip.poseVel.vel

        proxyUpdates.pollUntilEmpty { (pos, idealPos, vrPlayer) ->
            val localGrabPos = physShip.transform.shipToWorld.transformPosition(
                pos.toDouble().add(0.5, 0.5, 0.5), Vector3d())
            val idealPosDiff = idealPos.sub(localGrabPos, Vector3d())

            val posDif = idealPosDiff.mul(pConst, Vector3d())
            val mass = physShip.inertia.shipMass

            // Integrate
            posDif.sub(vel.mul(dConst, Vector3d()))

            val force = posDif.mul(mass, Vector3d())
            println("Applied FOrce: $force")
            physShip.applyInvariantForce(force)

            val quat = Quaterniond().rotateYXZ(
                toRadians(-vrPlayer.controller0.yaw.toDouble()),
                toRadians(-vrPlayer.controller0.pitch.toDouble()),
                toRadians(vrPlayer.controller0.roll.toDouble())
            ) ?: return@pollUntilEmpty

            val rotDiff = quat.mul(physShip.transform.shipToWorldRotation.invert(Quaterniond()), Quaterniond())
            val rotDiffVector = Vector3d(rotDiff.x() * 2.0, rotDiff.y() * 2.0, rotDiff.z() * 2.0).mul(pConstR)

            if (rotDiff.w < 0) rotDiffVector.mul(-1.0)
            rotDiffVector.mul(-1.0)

            // Integrate
            rotDiffVector.sub(physShip.poseVel.omega.mul(dConstR, Vector3d()))
            val torque = physShip.transform.shipToWorldRotation.transform(
                physShip.inertia.momentOfInertiaTensor.transform(
                    physShip.transform.shipToWorldRotation.transformInverse(
                        rotDiffVector, Vector3d())))
            println("Applied Torque: $torque")
            physShip.applyInvariantForce(torque)
        }
    }

    /**
     * Add proxy to be processed.
     * @param pos               the position of the proxy ([BlockPos]).
     * @param idealPos          the ideal position of the Ship ([Vector3d]).
     * @param vrPlayer          the instance of an IVRPlayer bound to the proxy ([IVRPlayer]).
     */
    fun addProxy(
        pos: BlockPos,
        idealPos: Vector3d,
        vrPlayer: IVRPlayer
    ) {
        proxyUpdates.add(ProxyUpdateData(pos.toJOML(), idealPos, vrPlayer))
    }

    companion object {
        val pConst = 160.0
        val dConst = 20.0
        val pConstR = 160.0
        val dConstR = 20.0

        /**
         * Gets or creates a VS ship attachment
         * @param ship the ship to apply to ([ServerShip]).
         */
        fun getOrCreate(ship: ServerShip) =
            ship.getAttachment<MarionettaShips>()
                ?: MarionettaShips().also {
                    ship.saveAttachment(it)
                }
    }
}