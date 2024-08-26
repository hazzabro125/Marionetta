package com.hazzabro124.marionetta.services

import com.hazzabro124.marionetta.registry.DeferredRegister
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey

interface DeferredRegisterBackend {
    fun <T> makeDeferredRegister(id: String, registry: ResourceKey<Registry<T>>): DeferredRegister<T>
}
