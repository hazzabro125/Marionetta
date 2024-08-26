package com.hazzabro124.marionetta.fabric;

import com.hazzabro124.marionetta.registry.DeferredRegister;
import com.hazzabro124.marionetta.registry.RegistrySupplier;
import kotlin.jvm.functions.Function0;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DeferredRegisterImpl<T> implements DeferredRegister<T> {
    private final String modId;
    private final Registry<T> registry;
    private final List<RegistrySupplier<T>> everMade = new ArrayList<>();

    @SuppressWarnings("all")
    public DeferredRegisterImpl(String modId, ResourceKey<Registry<T>> registry) {
        this.modId = modId;
        this.registry = (Registry<T>) Registry.REGISTRY.get(registry.location());
    }

    @NotNull
    @Override
    @SuppressWarnings("all")
    public <I extends T> RegistrySupplier<I> register(@NotNull String name, @NotNull Function0<? extends I> builder) {
        I result = Registry.register(registry, new ResourceLocation(modId, name), builder.invoke());

        RegistrySupplier<I> r = new RegistrySupplier<I>() {
            @NotNull
            @Override
            public String getName() {
                return name;
            }

            @Override
            public I get() {
                return result;
            }
        };

        everMade.add((RegistrySupplier<T>) r);
        return r;
    }

    @Override
    public void applyAll() {

    }

    @NotNull
    @Override
    public Iterator<RegistrySupplier<T>> iterator() {
        return everMade.iterator();
    }
}
