package com.hazzabro124.marionetta.items

import com.hazzabro124.marionetta.blocks.MarionettaBlocks.PROXY
import com.hazzabro124.marionetta.MarionettaMod.MOD_ID
import com.hazzabro124.marionetta.items.custom.LinkStickItem
import dev.architectury.registry.CreativeTabRegistry
import dev.architectury.registry.registries.DeferredRegister
import dev.architectury.registry.registries.RegistrySupplier
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.ItemStack

@Suppress("unused")
object MarionettaItems {
    private val ITEMS = DeferredRegister.create(MOD_ID, Registry.ITEM_REGISTRY)

    val TAB: CreativeModeTab =
        CreativeTabRegistry.create(ResourceLocation(MOD_ID, "main_tab"))
        { ItemStack(PROXY.get()) }

    val LINKSTICK: RegistrySupplier<LinkStickItem> = ITEMS.register("link_stick", ::LinkStickItem)

    /**
     * Registers the items in Marionetta
     */
    fun register() {
        ITEMS.register()
    }
}
