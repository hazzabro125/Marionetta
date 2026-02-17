package com.hazzabro124.marionetta.blocks

import com.hazzabro124.marionetta.MarionettaMod
import com.hazzabro124.marionetta.blocks.entity.ProxyBlockEntity
import dev.architectury.registry.registries.DeferredRegister
import dev.architectury.registry.registries.RegistrySupplier
import net.minecraft.core.Registry
import net.minecraft.core.registries.Registries
import net.minecraft.world.level.block.entity.BlockEntityType

@Suppress("unused")
object MarionettaBlockEntities {
    private val BLOCK_ENTITIES = DeferredRegister.create(MarionettaMod.MOD_ID, Registries.BLOCK_ENTITY_TYPE)

    val PROXY: RegistrySupplier<BlockEntityType<ProxyBlockEntity>> = BLOCK_ENTITIES.register("proxy") {
        BlockEntityType.Builder.of(::ProxyBlockEntity, MarionettaBlocks.PROXY.get()).build(null)
    }

    /**
     * Registers the block entities in Marionetta
     */
    fun register() {
        BLOCK_ENTITIES.register()
    }
}