package org.valkyrienskies.tournament

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import org.valkyrienskies.core.api.ships.getAttachment
import org.valkyrienskies.core.api.ships.saveAttachment
import org.valkyrienskies.core.impl.config.VSConfigClass
import org.valkyrienskies.core.impl.hooks.VSEvents
import org.valkyrienskies.tournament.ship.*
import org.valkyrienskies.tournament.util.extension.with

object TournamentMod {
    const val MOD_ID = "vs_tournament"

    @JvmStatic
    fun init() {
        VSConfigClass.registerConfig("vs_tournament", TournamentConfig::class.java)
        TournamentBlocks.register()
        TournamentItems.register()
        TournamentTriggers.init()
        TournamentOres.register()

        VSEvents.shipLoadEvent.on { e ->
            val ship = e.ship

            if (TournamentConfig.SERVER.removeAllAttachments) {
                ship.saveAttachment<BalloonShipControl>(null)
                ship.saveAttachment<SpinnerShipControl>(null)
                ship.saveAttachment<ThrusterShipControl>(null)
                ship.saveAttachment<TournamentShips>(null)
            }
            else {
                val thrusterShipCtrl = ship.getAttachment<ThrusterShipControl>()
                if (thrusterShipCtrl != null) {
                    TournamentShips.getOrCreate(ship).addThrusters(thrusterShipCtrl.Thrusters.with(thrusterShipCtrl.thrusters) as Iterable<TournamentShips.ImportedThrusterData>)
                    ship.saveAttachment<ThrusterShipControl>(null)
                }
            }
        }
    }

    @JvmStatic
    fun initClient() {

    }

    interface ClientRenderers {
        fun <T: BlockEntity> registerBlockEntityRenderer(t: BlockEntityType<T>, r: BlockEntityRendererProvider<T>)
    }

    @JvmStatic
    fun initClientRenderers(clientRenderers: ClientRenderers) {
    }
}
