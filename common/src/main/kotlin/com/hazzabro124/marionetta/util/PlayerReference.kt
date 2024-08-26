package com.hazzabro124.marionetta.util

import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer

class PlayerReference(
    private val id: Int
) {
    fun resolve(level: ServerLevel): ServerPlayer? =
        level.getEntity(id) as? ServerPlayer

    companion object {
        fun from(player: ServerPlayer): PlayerReference =
            PlayerReference(player.id)
    }
}