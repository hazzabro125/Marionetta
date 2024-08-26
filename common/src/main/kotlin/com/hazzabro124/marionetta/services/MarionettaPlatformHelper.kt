package com.hazzabro124.marionetta.services

import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.ItemStack
import java.util.ServiceLoader
import java.util.function.Supplier

interface MarionettaPlatformHelper {
    fun createCreativeTab(id: ResourceLocation, stack: Supplier<ItemStack>): CreativeModeTab

    companion object {
        fun get() = ServiceLoader
            .load(MarionettaPlatformHelper::class.java)
            .findFirst()
            .get()
    }
}