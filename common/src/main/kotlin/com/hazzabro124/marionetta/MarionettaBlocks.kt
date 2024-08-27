package com.hazzabro124.marionetta

import com.hazzabro124.marionetta.blocks.ProxyBlock
import com.hazzabro124.marionetta.registry.DeferredRegister
import com.hazzabro124.marionetta.registry.RegistrySupplier
import net.minecraft.core.Registry
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.*

@Suppress("unused")
object MarionettaBlocks {
    private val BLOCKS = DeferredRegister.create(MarionettaMod.MOD_ID, Registry.BLOCK_REGISTRY)
    private val ITEMS = ArrayList<Pair<String, ()-> Item>>()

    lateinit var PROXY  :RegistrySupplier<ProxyBlock>

    /**
     * Registers the blocks in Marionetta
     */
    fun register() {
        PROXY = register("proxy"){
            ProxyBlock()
        }

        BLOCKS.applyAll()
    }

    private fun <T: Block> register(name: String, block: () -> T): RegistrySupplier<T> {
        val supplier = BLOCKS.register(name, block)
        ITEMS.add(name to { BlockItem(supplier.get(), Item.Properties().tab(MarionettaItems.TAB)) })
        return supplier
    }

    /**
     * Registers the items associated with the blocks in Marionetta
     */
    fun registerItems(items: DeferredRegister<Item>) {
        ITEMS.forEach { items.register(it.first, it.second)}
    }
}