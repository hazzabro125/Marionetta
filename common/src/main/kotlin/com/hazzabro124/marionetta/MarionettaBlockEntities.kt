package com.hazzabro124.marionetta

import com.hazzabro124.marionetta.blocks.ProxyAnchor
import com.hazzabro124.marionetta.blocks.ProxyBlock
import com.hazzabro124.marionetta.blocks.entity.ProxyBlockEntity
import com.hazzabro124.marionetta.items.LinkStick
import com.hazzabro124.marionetta.registry.DeferredRegister
import com.hazzabro124.marionetta.registry.RegistrySupplier
import net.minecraft.core.Registry
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.*
import net.minecraft.world.level.block.entity.BlockEntityType

@Suppress("unused")
object MarionettaBlockEntities {
    private val BLOCK_ENTITIES = DeferredRegister.create(MarionettaMod.MOD_ID, Registry.BLOCK_ENTITY_TYPE_REGISTRY)

    val PROXY: RegistrySupplier<BlockEntityType<ProxyBlockEntity>> = BLOCK_ENTITIES.register("proxy") {
        BlockEntityType.Builder.of(::ProxyBlockEntity, MarionettaBlocks.PROXY.get()).build(null)
    }

    /**
     * Registers the blocks in Marionetta
     */
    fun register() {
        BLOCK_ENTITIES.applyAll()
    }
}