package com.hazzabro124.marionetta

import com.hazzabro124.marionetta.registry.DeferredRegister
import net.minecraft.core.Registry
import net.minecraft.world.item.CreativeModeTab

@Suppress("unused")
object MarionettaItems {
    private val ITEMS = DeferredRegister.create(MarionettaMod.MOD_ID, Registry.ITEM_REGISTRY)

    lateinit var TAB: CreativeModeTab

    fun register() {
        MarionettaBlocks.registerItems(ITEMS)
        ITEMS.applyAll()
    }
}
