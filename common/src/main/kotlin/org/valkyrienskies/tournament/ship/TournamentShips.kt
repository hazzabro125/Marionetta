package org.valkyrienskies.tournament.ship

import org.valkyrienskies.tournament.PIDController
import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonIgnore
import com.google.common.util.concurrent.AtomicDouble
import net.blf02.vrapi.api.data.IVRData
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.TextComponent
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.player.Player
import org.joml.Vector3d
import org.joml.Vector3i
import org.valkyrienskies.core.api.ships.*
import org.valkyrienskies.core.apigame.world.properties.DimensionId
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl
import org.valkyrienskies.mod.common.util.toJOML
import org.valkyrienskies.tournament.TickScheduler
import org.valkyrienskies.tournament.util.extension.toBlock
import org.valkyrienskies.tournament.util.extension.toDimensionKey
import org.valkyrienskies.tournament.util.extension.toDouble
import org.valkyrienskies.tournament.util.helper.Helper3d
import java.util.concurrent.CopyOnWriteArrayList
import net.minecraft.world.phys.Vec3
import org.valkyrienskies.tournament.TournamentCommands
import org.valkyrienskies.tournament.util.PlayerReference

@JsonAutoDetect(
    fieldVisibility = JsonAutoDetect.Visibility.ANY,
    getterVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE,
    setterVisibility = JsonAutoDetect.Visibility.NONE
)
class TournamentShips: ShipForcesInducer {

    var xkp: Double = 0.0
    var level: DimensionId = "minecraft:overworld"
    lateinit var globalLevel: ServerLevel

    data class ThrusterData(
        val pos: Vector3i,
        val force: Vector3d,
        val mult: Double,
        var submerged: Boolean,
        var boundPlayer: PlayerReference
    )

    data class ImportedThrusterData(
        val pos: Vector3i,
        val force: Vector3d,
        val tier: Double,
        val boundPlayer: PlayerReference
    )

    val thrusters =
        CopyOnWriteArrayList<ThrusterData>()

    private val balloons =
        CopyOnWriteArrayList<Pair<Vector3i, Double>>()

    private val spinners =
        CopyOnWriteArrayList<Pair<Vector3i, Vector3d>>()

    private val pulses =
        CopyOnWriteArrayList<Pair<Vector3d, Vector3d>>()

    data class PropellerData(
        val pos: Vector3i,
        val force: Vector3d,
        var speed: AtomicDouble,
        var touchingWater: Boolean
    )

    private val propellers =
        CopyOnWriteArrayList<PropellerData>()

    @JsonIgnore
    private var ticker: TickScheduler.Ticking? = null

    fun IVRData.relativePosition(boundPlayer: Player): Vec3 {
        return this.position().subtract(boundPlayer.position())
    }



    override fun applyForces(physShip: PhysShip) {
        physShip as PhysShipImpl

        if (ticker == null) {
            ticker = TickScheduler.serverTickPerm { server ->
                val lvl = server.getLevel(level.toDimensionKey())
                    globalLevel = lvl
                    ?: return@serverTickPerm

                thrusters.forEach { t ->
                    val water = lvl.isWaterAt(
                        Helper3d
                            .convertShipToWorldSpace(lvl, t.pos.toDouble())
                            .toBlock()
                    )
                    t.submerged = water
                }
            }
        }

        val vel = physShip.poseVel.vel

        thrusters.forEach { data ->
            val (pos, force, tier, submerged, boundplayer) = data
            var boundplayer2 = boundplayer.resolve(globalLevel)

            val tPos = Helper3d.convertShipToWorldSpace(globalLevel, pos.toDouble())
           // val tForce1 = physShip.transform.shipToWorld.transformDirection(force, Vector3d())
         //   var tForce2: Vector3d = ((VRPlugin.vrAPI!!.getVRPlayer(boundplayer).controller0.lookAngle)).toJOML()
         //   var tRelative: Vector3d = VRPlugin.vrAPI!!.getVRPlayer(boundplayer).controller0.relativePosition(boundplayer).toJOML()
         //   var tRelativeMechPos: Vector3d = tRelative.mul(2.0)
        //    var tMechPos: Vector3d = tRelativeMechPos.add(boundplayer.position().toJOML())
            var tMechPos2: Vector3d = boundplayer.resolve(globalLevel)!!.position().toJOML().add(0.0, 3.0, 0.0)
            var tPID: Vector3d = Vector3d(PIDController(kp = TournamentCommands.xkp, ki = TournamentCommands.xki, kd = TournamentCommands.xkd,).calculateOutput(tPos.x, tMechPos2.x),
                PIDController(kp = TournamentCommands.ykp, ki = TournamentCommands.yki, kd = TournamentCommands.ykd,).calculateOutput(tPos.y, tMechPos2.y),
                PIDController(kp = TournamentCommands.zkp, ki = TournamentCommands.zki, kd = TournamentCommands.zkd,).calculateOutput(tPos.z, tMechPos2.z))

            boundplayer2!!.sendMessage(TextComponent("Bound to ${boundplayer2.name.contents}"), boundplayer2.uuid)
            boundplayer2.sendMessage(TextComponent("$tMechPos2"), boundplayer2.uuid)
            boundplayer2.sendMessage(TextComponent("$tPID"), boundplayer2.uuid)
            boundplayer2.sendMessage(TextComponent("$tPos"), boundplayer2.uuid)

            physShip.applyInvariantForce(tPID)

        }
    }

    fun addThruster(
        pos: BlockPos,
        tier: Double,
        force: Vector3d,
        boundPlayer: PlayerReference
    ) {
        thrusters += ThrusterData(pos.toJOML(), force, tier, false, boundPlayer)
    }

    fun addThrusters(
        list: Iterable<ImportedThrusterData>
    ) {
        list.forEach { (pos, force, tier, boundPlayer) ->
            thrusters += ThrusterData(pos, force, tier, false, boundPlayer)
        }
    }

    fun stopThruster(
        pos: BlockPos
    ) {
        thrusters.removeIf { pos.toJOML() == it.pos }
    }

    companion object {
        fun getOrCreate(ship: ServerShip, level: DimensionId) =
            ship.getAttachment<TournamentShips>()
                ?: TournamentShips().also {
                    it.level = level
                    ship.saveAttachment(it)
                }

        fun getOrCreate(ship: ServerShip): TournamentShips =
            getOrCreate(ship, ship.chunkClaimDimension)
    }
}
