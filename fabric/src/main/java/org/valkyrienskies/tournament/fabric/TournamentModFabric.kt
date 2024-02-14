package org.valkyrienskies.tournament.fabric

import com.mojang.brigadier.CommandDispatcher
import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.commands.CommandSourceStack
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import org.valkyrienskies.core.impl.config.VSConfigClass.Companion.getRegisteredConfig
import org.valkyrienskies.mod.compat.clothconfig.VSClothConfig.createConfigScreenFor
import org.valkyrienskies.mod.fabric.common.ValkyrienSkiesModFabric
import org.valkyrienskies.tournament.TournamentCommands.register
import org.valkyrienskies.tournament.TournamentConfig
import org.valkyrienskies.tournament.TournamentItems
import org.valkyrienskies.tournament.TournamentMod
import org.valkyrienskies.tournament.TournamentMod.ClientRenderers
import org.valkyrienskies.tournament.TournamentMod.init
import org.valkyrienskies.tournament.TournamentMod.initClient
import org.valkyrienskies.tournament.TournamentMod.initClientRenderers
import org.valkyrienskies.tournament.TournamentModels.MODELS
import java.util.function.Consumer

class TournamentModFabric : ModInitializer {
    override fun onInitialize() {
        // force VS2 to load before Tournament
        ValkyrienSkiesModFabric().onInitialize()

        TournamentItems.TAB = FabricItemGroupBuilder
            .create(ResourceLocation(TournamentMod.MOD_ID, "main_tab"))
            .build()

        CommandRegistrationCallback.EVENT.register(CommandRegistrationCallback { dispatcher: CommandDispatcher<CommandSourceStack?>?, dedicated: Boolean ->
            register(
                dispatcher!!
            )
        })


        init()
    }

    @Environment(EnvType.CLIENT)
    class Client : ClientModInitializer {
        override fun onInitializeClient() {
            initClient()
            initClientRenderers(ClientRenderersFabric())

            ModelLoadingRegistry.INSTANCE.registerModelProvider { manager: ResourceManager?, out: Consumer<ResourceLocation?>? ->
                MODELS.forEach(
                    out
                )
            }
        }

        private class ClientRenderersFabric : ClientRenderers {
            override fun <T : BlockEntity> registerBlockEntityRenderer(
                t: BlockEntityType<T>,
                r: BlockEntityRendererProvider<T>
            ) {
                BlockEntityRendererRegistry.register(t, r)
            }
        }
    }

    class ModMenu : ModMenuApi {
        override fun getModConfigScreenFactory(): ConfigScreenFactory<*> {
            return ConfigScreenFactory { parent: Screen? ->
                createConfigScreenFor(
                    parent!!,
                    getRegisteredConfig(TournamentConfig::class.java)
                )
            }
        }
    }
}
