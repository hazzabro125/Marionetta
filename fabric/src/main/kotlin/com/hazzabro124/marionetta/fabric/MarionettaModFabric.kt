package com.hazzabro124.marionetta.fabric;

import com.hazzabro124.marionetta.MarionettaConfig
import com.hazzabro124.marionetta.MarionettaMod.init
import com.hazzabro124.marionetta.MarionettaMod.initClient
import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.api.ModInitializer
import net.minecraft.client.gui.screens.Screen
import org.valkyrienskies.core.impl.config.VSConfigClass.Companion.getRegisteredConfig
import org.valkyrienskies.mod.compat.clothconfig.VSClothConfig.createConfigScreenFor
import org.valkyrienskies.mod.fabric.common.ValkyrienSkiesModFabric

class MarionettaModFabric : ModInitializer {
    override fun onInitialize() {
        ValkyrienSkiesModFabric().onInitialize()

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
