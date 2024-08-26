package com.hazzabro124.marionetta.fabric.services;

import com.hazzabro124.marionetta.services.MarionettaPlatformHelper;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class MarionettaPlatformHelperFabric implements MarionettaPlatformHelper {
    @NotNull
    @Override
    public CreativeModeTab createCreativeTab(@NotNull ResourceLocation id, @NotNull Supplier<ItemStack> stack) {
        return FabricItemGroupBuilder
                .create(id)
                .icon(stack)
                .build();
    }
}
