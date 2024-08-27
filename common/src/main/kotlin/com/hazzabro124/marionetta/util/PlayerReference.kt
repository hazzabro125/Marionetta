package com.hazzabro124.marionetta.util

import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer

/**
 * A reference to a player
 * @param id the player's id ([Int]).
 */
class PlayerReference(
    private val id: Int
) {
    /**
     * Attempts to return a [ServerPlayer] from a [PlayerReference]
     * @param level the level to search for the player in ([ServerLevel]).
     */
    fun resolve(level: ServerLevel): ServerPlayer? =
        level.getEntity(id) as? ServerPlayer

    companion object {
        /**
         * Creates a [PlayerReference] from a player
         * @param player the player to reference ([ServerPlayer]).
         */
        fun from(player: ServerPlayer): PlayerReference =
            PlayerReference(player.id)
    }
}