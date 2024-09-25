package com.hazzabro124.marionetta.ship

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.hazzabro124.marionetta.util.extension.toDouble
import net.blf02.vrapi.api.data.IVRData
import net.blf02.vrapi.api.data.IVRPlayer
import net.blf02.vrapi.data.VRPlayer
import net.minecraft.core.BlockPos
import net.minecraft.util.StringRepresentable
import org.joml.Quaterniond
import org.joml.Vector3d
import org.joml.Vector3i
import org.valkyrienskies.core.api.ships.*
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl
import org.valkyrienskies.core.util.pollUntilEmpty
import org.valkyrienskies.mod.common.util.toJOML
import java.lang.Math.toRadians
import java.util.concurrent.ConcurrentLinkedQueue

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
     * @property vrPlayer       The bound VRPlayer instance ([IVRPlayer])
     * @property controllerType The selected controller ([ControllerTypeEnum]).
     * @property anchorPos      The position of the proxy's linked anchor or null ([Vector3i])
     */
    data class ProxyUpdateData(val pos: Vector3i, val vrPlayer: IVRPlayer, val controllerType: ControllerTypeEnum, val anchorPos: Vector3i?)

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

        proxyUpdates.pollUntilEmpty { (pos, vrPlayer, controllerType, anchorPos) ->
            val controller = when(controllerType) {
                ControllerTypeEnum.controller0 -> vrPlayer.controller0
                else -> vrPlayer.controller1
            }

            val headQuat = Quaterniond().rotateYXZ(
                toRadians(-vrPlayer.hmd.yaw.toDouble()),
                toRadians(vrPlayer.hmd.pitch.toDouble()),
                toRadians(0.0),
            )

            val localXOffset = if (controllerType == ControllerTypeEnum.controller1) xOffset * -1 else xOffset

            val idealPos: Vector3d =
                controller.position().toJOML()
                    .sub(vrPlayer.hmd.position().toJOML())
                    .add(headQuat.transform(Vector3d(localXOffset, yOffset, zOffset)))
                    .mul(scale)
                    .add(vrPlayer.hmd.position().toJOML())

            val localGrabPos = physShip.transform.shipToWorld.transformPosition(
                pos.toDouble().add(0.5, 0.5, 0.5), Vector3d())
            val idealPosDiff = idealPos.sub(localGrabPos, Vector3d())

            val posDif = idealPosDiff.mul(pConst, Vector3d())
            val mass = physShip.inertia.shipMass

            // Integrate
            posDif.sub(vel.mul(dConst, Vector3d()))

            val force = posDif.mul(mass, Vector3d())
            println("Applied Force: $force")
            physShip.applyInvariantForce(force)



            val handQuat = Quaterniond().rotateYXZ(
                toRadians(-controller.yaw.toDouble()),
                toRadians(-controller.pitch.toDouble()),
                toRadians(controller.roll.toDouble())
            ) ?: return@pollUntilEmpty

            val rotDiff = handQuat.mul(physShip.transform.shipToWorldRotation.invert(Quaterniond()), Quaterniond())
                .normalize().invert()
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
     * @param vrPlayer          The bound VRPlayer instance ([IVRPlayer])
     * @param controllerType    The selected controller ([ControllerTypeEnum]).
     * @param anchorPos         the position of the proxy's linked anchor or null ([Vector3i])
     */
    fun addProxy(
        pos: BlockPos,
        vrPlayer: IVRPlayer,
        controllerType: ControllerTypeEnum,
        anchorPos: BlockPos?
    ) {
        proxyUpdates.add(ProxyUpdateData(pos.toJOML(), vrPlayer, controllerType, anchorPos?.toJOML()))
    }

    companion object {
        val scale = 4.0
        val xOffset = -0.25
        val yOffset = 0.25
        val zOffset = 0.0

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