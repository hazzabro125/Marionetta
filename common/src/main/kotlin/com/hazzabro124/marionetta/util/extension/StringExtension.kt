package com.hazzabro124.marionetta.util.extension

import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import org.valkyrienskies.core.apigame.world.properties.DimensionId

fun DimensionId.toDimensionKey() =
    this.split(":").let {
        ResourceLocation(it[it.size - 2], it[it.size -1]).toDimensionKey()
    }

fun ResourceLocation.toDimensionKey() =
    ResourceKey.create(Registry.DIMENSION_REGISTRY, this)