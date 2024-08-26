package com.hazzabro124.marionetta

import org.joml.Quaterniondc
import org.joml.Vector3dc

data class GravitronForceInducerData(
    val idealPos: Vector3dc,
    val idealRot: Quaterniondc,
    val grabbedPos: Vector3dc,
)