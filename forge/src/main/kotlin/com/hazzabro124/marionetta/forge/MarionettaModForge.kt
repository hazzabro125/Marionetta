package com.hazzabro124.marionetta.forge

import com.hazzabro124.marionetta.MarionettaMod
import com.hazzabro124.marionetta.MarionettaMod.MOD_ID
import com.hazzabro124.marionetta.MarionettaMod.init
import com.hazzabro124.marionetta.MarionettaMod.initClient
import com.hazzabro124.marionetta.MarionettaMod.initClientRenderers
import dev.architectury.platform.forge.EventBuses
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import thedarkcolour.kotlinforforge.forge.MOD_BUS

@Mod(MOD_ID)
class MarionettaModForge {
    private var happendClientSetup = false

    init {
        EventBuses.registerModEventBus(MOD_ID, MOD_BUS)
        val forgeBus = Mod.EventBusSubscriber.Bus.FORGE.bus().get()

        MOD_BUS.addListener { event: FMLClientSetupEvent? ->
            clientSetup(
                event
            )
        }

        MOD_BUS.addListener { event: RegisterRenderers ->
            entityRenderers(
                event
            )
        }

        init()
    }

    private fun clientSetup(event: FMLClientSetupEvent?) {
        if (happendClientSetup) {
            return
        }

        happendClientSetup = true
        initClient()
    }

    private fun entityRenderers(event: RegisterRenderers) {
        initClientRenderers(
            object: MarionettaMod.ClientRenderers {
                override fun <T : BlockEntity> registerBlockEntityRenderer(
                    t: BlockEntityType<T>,
                    r: BlockEntityRendererProvider<T>
                ) = event.registerBlockEntityRenderer(t, r)
            }
        )
    }

    companion object {
        fun getModBus(): IEventBus = MOD_BUS
    }
}