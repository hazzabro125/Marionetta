package com.hazzabro124.marionetta

import com.hazzabro124.marionetta.blocks.MarionettaBlockEntities
import com.hazzabro124.marionetta.blocks.MarionettaBlocks
import com.hazzabro124.marionetta.items.MarionettaItems
import com.hazzabro124.marionetta.ship.MarionettaShips
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import org.valkyrienskies.mod.common.ValkyrienSkiesMod.vsCore

object MarionettaMod {
    const val MOD_ID = "marionetta"

    @JvmStatic
    fun init() {
        //VSConfigApi.registerConfig(MOD_ID, MarionettaConfig::class.java)
        vsCore.registerAttachment(MarionettaShips::class.java)

        MarionettaBlocks.register()
        MarionettaItems.register()
        MarionettaBlockEntities.register()
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