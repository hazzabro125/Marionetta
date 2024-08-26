package com.hazzabro124.marionetta.fabric;

import com.hazzabro124.marionetta.MarionettaConfig
import com.hazzabro124.marionetta.MarionettaItems
import com.hazzabro124.marionetta.MarionettaMod
import com.hazzabro124.marionetta.MarionettaMod.init
import com.hazzabro124.marionetta.MarionettaMod.initClient
import com.mojang.brigadier.CommandDispatcher
import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import dev.architectury.utils.Env
import me.shedaniel.clothconfig2.api.ConfigScreen
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback
import net.minecraft.client.gui.screens.Screen
import net.minecraft.commands.CommandSourceStack
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import org.valkyrienskies.core.impl.config.VSConfigClass.Companion.getRegisteredConfig
import org.valkyrienskies.mod.compat.clothconfig.VSClothConfig.createConfigScreenFor
import org.valkyrienskies.mod.fabric.common.ValkyrienSkiesModFabric
import java.util.function.Consumer

public class MarionettaModFabric : ModInitializer {
    override fun onInitialize() {
        ValkyrienSkiesModFabric().onInitialize()

        MarionettaItems.TAB = FabricItemGroupBuilder
            .create(ResourceLocation(MarionettaMod.MOD_ID, "main_tab"))
            .build()

        init()
    }

    @Environment(EnvType.CLIENT)
    class Client : ClientModInitializer {
        override fun onInitializeClient() {
            initClient()
        }
    }

    class ModMenu : ModMenuApi {
        override fun getModConfigScreenFactory(): ConfigScreenFactory<*> {
            return ConfigScreenFactory { parent: Screen? ->
                createConfigScreenFor(
                    parent!!,
                    getRegisteredConfig(MarionettaConfig::class.java)
                )
            }
        }
    }
}
