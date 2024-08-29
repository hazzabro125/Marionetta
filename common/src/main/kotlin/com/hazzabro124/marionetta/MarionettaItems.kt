package com.hazzabro124.marionetta

import com.hazzabro124.marionetta.blocks.ProxyAnchor
import com.hazzabro124.marionetta.items.LinkStick
import com.hazzabro124.marionetta.registry.DeferredRegister
import com.hazzabro124.marionetta.registry.RegistrySupplier
import net.minecraft.core.Registry
import net.minecraft.world.item.CreativeModeTab

@Suppress("unused")
object MarionettaItems {
    private val ITEMS = DeferredRegister.create(MarionettaMod.MOD_ID, Registry.ITEM_REGISTRY)

    lateinit var TAB: CreativeModeTab

    lateinit var LINKSTICK : RegistrySupplier<LinkStick>

    /**
     * Registers the items in Marionetta
     */
    fun register() {
        LINKSTICK = ITEMS.register("linkstick", ::LinkStick)
        MarionettaBlocks.registerItems(ITEMS)
        ITEMS.applyAll()
    }
}
