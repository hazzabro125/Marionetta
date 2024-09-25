package com.hazzabro124.marionetta.blocks

import com.hazzabro124.marionetta.items.MarionettaItems
import com.hazzabro124.marionetta.MarionettaMod
import com.hazzabro124.marionetta.blocks.custom.ProxyAnchorBlock
import com.hazzabro124.marionetta.blocks.custom.ProxyBlock
import dev.architectury.registry.registries.DeferredRegister
import dev.architectury.registry.registries.RegistrySupplier
import net.minecraft.core.Registry
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item

@Suppress("unused")
object MarionettaBlocks {
    private val BLOCKS = DeferredRegister.create(MarionettaMod.MOD_ID, Registry.BLOCK_REGISTRY)
    private val ITEMS = DeferredRegister.create(MarionettaMod.MOD_ID, Registry.ITEM_REGISTRY)

    val PROXY: RegistrySupplier<ProxyBlock> = BLOCKS.register("proxy") { ProxyBlock() }
    val ANCHOR: RegistrySupplier<ProxyAnchorBlock> = BLOCKS.register("anchor") { ProxyAnchorBlock() }

    /**
     * Registers the blocks in Marionetta
     */
    fun register() {
        BLOCKS.register()

        BLOCKS.forEach { block ->
            ITEMS.register(block.id) {
                BlockItem(block.get(), Item.Properties().tab(MarionettaItems.TAB))
            }
        }

        ITEMS.register()
    }
}