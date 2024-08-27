package com.hazzabro124.marionetta

import com.hazzabro124.marionetta.ship.MarionettaShips.ControllerTypeEnum
import net.minecraft.world.level.block.state.properties.EnumProperty

/**
 * Defines custom properties
 */
object MarionettaProperties {
    /**
     * [EnumProperty] of [ControllerTypeEnum]. Specifies the VR Controller
     */
    val CONTROLLER: EnumProperty<ControllerTypeEnum> = EnumProperty.create("controller", ControllerTypeEnum::class.java)
}