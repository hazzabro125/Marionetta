package com.hazzabro124.marionetta.forge.services;

import com.hazzabro124.marionetta.services.MarionettaPlatformHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class MarionettaPlatformHelperForge implements MarionettaPlatformHelper {
    @NotNull
    @Override
    public CreativeModeTab createCreativeTab(@NotNull ResourceLocation id, @NotNull Supplier<ItemStack> stack) {
        return new CreativeModeTab(id.toString().replace(":",".")) {
            @Override
            public ItemStack makeIcon() {
                return stack.get();
            }

            @Override
            public Component getDisplayName() {
                return new TranslatableComponent("itemGroup." + String.format("%s.%s",id.getNamespace(), id.getPath()));
            }
        };
    }
}
