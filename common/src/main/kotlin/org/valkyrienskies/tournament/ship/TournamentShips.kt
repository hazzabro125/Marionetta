package org.valkyrienskies.tournament.ship

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonIgnore
import com.google.common.util.concurrent.AtomicDouble
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.player.Player
import org.joml.Vector3d
import org.joml.Vector3i
import org.valkyrienskies.core.api.ships.*
import org.valkyrienskies.core.apigame.world.properties.DimensionId
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl
import org.valkyrienskies.mod.common.util.toJOML
import org.valkyrienskies.tournament.TickScheduler
import org.valkyrienskies.tournament.TournamentConfig
import org.valkyrienskies.tournament.util.extension.toBlock
import org.valkyrienskies.tournament.util.extension.toDimensionKey
import org.valkyrienskies.tournament.util.extension.toDouble
import org.valkyrienskies.tournament.util.helper.Helper3d
import java.util.concurrent.CopyOnWriteArrayList
import net.minecraft.world.phys.Vec3

@JsonAutoDetect(
    fieldVisibility = JsonAutoDetect.Visibility.ANY,
    getterVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE,
    setterVisibility = JsonAutoDetect.Visibility.NONE
)
class TournamentShips: ShipForcesInducer {

    var level: DimensionId = "minecraft:overworld"

    data class ThrusterData(
        val pos: Vector3i,
        val force: Vector3d,
        val mult: Double,
        var submerged: Boolean,
        var boundPlayer: Player
    )

    data class ImportedThrusterData(
        val pos: Vector3i,
        val force: Vector3d,
        val tier: Double,
        val boundPlayer: Player
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

    override fun applyForces(physShip: PhysShip) {
        physShip as PhysShipImpl

        if (ticker == null) {
            ticker = TickScheduler.serverTickPerm { server ->
                val lvl = server.getLevel(level.toDimensionKey())
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

            if (submerged) {
                return@forEach
            }

            val tForce: Vec3 = Vec3(0.0,1.0,0.0)

 /*           val tForce: Vec3 = when (VRPlugin.vrAPI?.getVRPlayer(boundplayer)) {
                null -> Vec3(0.0,-1.0,0.0)
              else -> VRPlugin.vrAPI!!.getVRPlayer(boundplayer).controller0.position()
            }
*/
            val tPos = pos.toDouble().add(0.5, 0.5, 0.5).sub(physShip.transform.positionInShip)

            if (force.isFinite && (
                TournamentConfig.SERVER.thrusterShutoffSpeed == -1.0
                    || physShip.poseVel.vel.length() < TournamentConfig.SERVER.thrusterShutoffSpeed
                )
            ) {
                physShip.applyInvariantForce(tForce.toJOML().mul(5.0))
            }
        }
    }

    fun addThruster(
        pos: BlockPos,
        tier: Double,
        force: Vector3d,
        boundPlayer: Player
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