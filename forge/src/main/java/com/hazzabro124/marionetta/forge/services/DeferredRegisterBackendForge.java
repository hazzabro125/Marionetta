package com.hazzabro124.marionetta.forge.services;

import com.hazzabro124.marionetta.forge.DeferredRegisterImpl;
import com.hazzabro124.marionetta.registry.DeferredRegister;
import com.hazzabro124.marionetta.services.DeferredRegisterBackend;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.NotNull;

public class DeferredRegisterBackendForge implements DeferredRegisterBackend {
    @NotNull
    @Override
    public <T> DeferredRegister<T> makeDeferredRegister(@NotNull String id, @NotNull ResourceKey<Registry<T>> registry) {
        return new DeferredRegisterImpl(id, registry);
    }
}
