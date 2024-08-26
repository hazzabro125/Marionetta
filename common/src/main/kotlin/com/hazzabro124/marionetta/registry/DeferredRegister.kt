package com.hazzabro124.marionetta.registry

import com.hazzabro124.marionetta.services.DeferredRegisterBackend
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import java.util.ServiceLoader

interface DeferredRegister<T>: Iterable<RegistrySupplier<T>> {
    fun <I: T> register(name: String, builder: () -> I): RegistrySupplier<I>
    fun applyAll()

    companion object {
        private val backend: DeferredRegisterBackend = load()

        fun <T> create(id: String, registry: ResourceKey<Registry<T>>): DeferredRegister<T> =
            backend.makeDeferredRegister(id, registry)

        public fun load(): DeferredRegisterBackend =
            ServiceLoader.load(DeferredRegisterBackend::class.java)
                .findFirst()
                .orElseThrow { NullPointerException("Failed to load service for DeferredRegisterBackend")}
    }
}