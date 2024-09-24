package com.hazzabro124.marionetta

import com.hazzabro124.marionetta.ship.MarionettaShips
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import org.valkyrienskies.core.api.ships.saveAttachment
import org.valkyrienskies.core.impl.config.VSConfigClass
import org.valkyrienskies.core.impl.hooks.VSEvents

object MarionettaMod {
    const val MOD_ID = "marionetta"

    @JvmStatic
    fun init() {
        VSConfigClass.registerConfig(MOD_ID, MarionettaConfig::class.java)
        MarionettaBlocks.register()
        MarionettaItems.register()
        MarionettaBlockEntities.register()

        VSEvents.shipLoadEvent.on { e ->
            val ship = e.ship

            if (MarionettaConfig.SERVER.removeAllAttachments){
                ship.saveAttachment<MarionettaShips>(null)
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