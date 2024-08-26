package com.hazzabro124.marionetta.forge

import com.hazzabro124.marionetta.MarionettaBlocks.PROXY
import com.hazzabro124.marionetta.MarionettaConfig
import com.hazzabro124.marionetta.MarionettaItems
import com.hazzabro124.marionetta.MarionettaMod
import com.hazzabro124.marionetta.MarionettaMod.init
import com.hazzabro124.marionetta.MarionettaMod.initClient
import com.hazzabro124.marionetta.MarionettaMod.initClientRenderers
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraftforge.client.ConfigGuiHandler.ConfigGuiFactory
import net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers
import net.minecraftforge.client.event.ModelRegistryEvent
import net.minecraftforge.event.RegistryEvent.Register
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import org.valkyrienskies.core.impl.config.VSConfigClass.Companion.getRegisteredConfig
import org.valkyrienskies.mod.compat.clothconfig.VSClothConfig.createConfigScreenFor
import thedarkcolour.kotlinforforge.forge.LOADING_CONTEXT
import thedarkcolour.kotlinforforge.forge.MOD_BUS

@Mod(MarionettaMod.MOD_ID)
class MarionettaModForge {
    private var happendClientSetup = false

    init {
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

        LOADING_CONTEXT.registerExtensionPoint(
            ConfigGuiFactory::class.java
        ){
            ConfigGuiFactory { _: Minecraft?, parent: Screen? ->
                createConfigScreenFor(
                    parent!!,
                    getRegisteredConfig(MarionettaConfig::class.java)
                )
            }
        }
        MarionettaItems.TAB = object : CreativeModeTab("marionetta.main_tab"){
            override fun makeIcon(): ItemStack {
                return ItemStack(PROXY.get())
            }
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