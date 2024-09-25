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
import org.valkyrienskies.mod.common.util.toBlockPos
import org.valkyrienskies.mod.common.util.toJOML
import org.valkyrienskies.mod.common.util.toJOMLD
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
    data class ProxyUpdateData(val pos: Vector3i, val vrPlayer: IVRPlayer, val controllerType: ControllerTypeEnum, val anchorPos: Vector3i?, val anchorDirection: Vector3d?, val settings: ProxySettings = ProxySettings())

    /**
     * Data class for proxy block settings
     * @property scale
     * @property xOffset
     * @property yOffset
     * @property zOffset
     */
    data class ProxySettings(val scale: Double = 4.0, val xOffset: Double = -0.25, val yOffset: Double = 0.25, val zOffset: Double = 0.0)

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

        proxyUpdates.pollUntilEmpty { (pos, vrPlayer, controllerType, anchorPos, anchorDirection, settings) ->
            val controller = when(controllerType) {
                ControllerTypeEnum.controller0 -> vrPlayer.controller0
                else -> vrPlayer.controller1
            }

            val headQuat = Quaterniond()
            anchorDirection?.let { direction ->
                headQuat.rotateYXZ(
                    toRadians(-direction.y),
                    toRadians(direction.x),
                    toRadians(direction.z)
                )
            } ?: headQuat.rotateYXZ(
                toRadians(-vrPlayer.hmd.yaw.toDouble()),
                toRadians(vrPlayer.hmd.pitch.toDouble()),
                toRadians(0.0)
            )

            val idealPos: Vector3d =
                controller.position().toJOML()
                    .sub(vrPlayer.hmd.position().toJOML())
                    .add(headQuat.transform(Vector3d(settings.xOffset, settings.yOffset, settings.zOffset)))
                    .mul(settings.scale)
                    .add(anchorPos?.toDouble()?.add(0.5, 0.5, 0.5) ?: vrPlayer.hmd.position().toJOML())

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



            var handQuat = Quaterniond().rotateYXZ(
                toRadians(-controller.yaw.toDouble()),
                toRadians(-controller.pitch.toDouble()),
                toRadians(controller.roll.toDouble())
            ) ?: return@pollUntilEmpty

            if (anchorDirection != null) {
                handQuat = handQuat.difference(headQuat).add(Quaterniond().rotateYXZ(
                    toRadians(-anchorDirection.y),
                    toRadians(anchorDirection.x),
                    toRadians(anchorDirection.z)
                ))
            }

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
            physShip.applyInvariantTorque(torque)
        }
    }

    /**
     * Add proxy to be processed.
     * @param pos               the position of the proxy ([BlockPos]).
     * @param vrPlayer          The bound VRPlayer instance ([IVRPlayer])
     * @param controllerType    The selected controller ([ControllerTypeEnum]).
     * @param anchorPos         the position of the proxy's linked anchor or null ([Vector3i])
     * @param settings          the settings for the proxy ([ProxySettings])
     */
    fun addProxy(
        pos: BlockPos,
        vrPlayer: IVRPlayer,
        controllerType: ControllerTypeEnum,
        anchorPos: BlockPos? = null,
        anchorDirection: Vector3d? = null,
        settings: ProxySettings = ProxySettings()
    ) {
        proxyUpdates.add(ProxyUpdateData(pos.toJOML(), vrPlayer, controllerType, anchorPos?.toJOML(), anchorDirection, settings))
    }

    companion object {
        const val pConst = 160.0
        const val dConst = 20.0
        const val pConstR = 160.0
        const val dConstR = 20.0

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