package org.valkyrienskies.tournament

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.Util
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.Commands.literal
import net.minecraft.network.chat.TextComponent
import net.minecraft.world.entity.player.Player
import org.valkyrienskies.tournament.registry.DeferredRegister
import net.minecraft.core.Registry
import org.valkyrienskies.mod.mixinducks.feature.command.VSCommandSource
import org.valkyrienskies.tournament.ship.TournamentShips


object TournamentCommands {
    var xkp: Double = 25000.0
    var xki: Double = 0.0
    var xkd: Double = 0.0

    var ykp: Double = 50000.0
    var yki: Double = 0.0
    var ykd: Double = 0.0

    var zkp: Double = 25000.0
    var zki: Double = 0.0
    var zkd: Double = 0.0

    fun register(dispatcher: CommandDispatcher<CommandSourceStack?>) {
        dispatcher as CommandDispatcher<*>

        dispatcher.register(
            literal("tournament")
                .then(literal("x")
                .then(literal("kp").executes {
                    try {
                        val r = xkp
                        it.source.playerOrException.sendMessage(TextComponent("$r"), Util.NIL_UUID)
                        1
                    } catch (e: Exception) {
                        throw e
                    }
                }.then(argument("value", DoubleArgumentType.doubleArg()).executes {
                    try {
                        val r = DoubleArgumentType.getDouble(it, "value")
                        xkp = r
                        it.source.playerOrException.sendMessage(TextComponent("$r"), Util.NIL_UUID)
                        1
                    } catch (e: Exception) {
                        throw e
                    }
                })).then(literal("ki").executes {
                        try {
                            val r = xki
                            it.source.playerOrException.sendMessage(TextComponent("$r"), Util.NIL_UUID)
                            1
                        } catch (e: Exception) {
                            throw e
                        }
                    }.then(argument("value", DoubleArgumentType.doubleArg()).executes {
                        try {
                            val r = DoubleArgumentType.getDouble(it, "value")
                            xki = r
                            it.source.playerOrException.sendMessage(TextComponent("$r"), Util.NIL_UUID)
                            1
                        } catch (e: Exception) {
                            throw e
                        }
                    })).then(literal("kd").executes {
                        try {
                            val r = xkd
                            it.source.playerOrException.sendMessage(TextComponent("$r"), Util.NIL_UUID)
                            1
                        } catch (e: Exception) {
                            throw e
                        }
                    }.then(argument("value", DoubleArgumentType.doubleArg()).executes {
                        try {
                            val r = DoubleArgumentType.getDouble(it, "value")
                            xkd = r
                            it.source.playerOrException.sendMessage(TextComponent("$r"), Util.NIL_UUID)
                            1
                        } catch (e: Exception) {
                            throw e
                        }
                    }))
                ).then(literal("y")
                    .then(literal("kp").executes {
                        try {
                            val r = ykp
                            it.source.playerOrException.sendMessage(TextComponent("$r"), Util.NIL_UUID)
                            1
                        } catch (e: Exception) {
                            throw e
                        }
                    }.then(argument("value", DoubleArgumentType.doubleArg()).executes {
                        try {
                            val r = DoubleArgumentType.getDouble(it, "value")
                            ykp = r
                            it.source.playerOrException.sendMessage(TextComponent("$r"), Util.NIL_UUID)
                            1
                        } catch (e: Exception) {
                            throw e
                        }
                    })).then(literal("ki").executes {
                        try {
                            val r = yki
                            it.source.playerOrException.sendMessage(TextComponent("$r"), Util.NIL_UUID)
                            1
                        } catch (e: Exception) {
                            throw e
                        }
                    }.then(argument("value", DoubleArgumentType.doubleArg()).executes {
                        try {
                            val r = DoubleArgumentType.getDouble(it, "value")
                            yki = r
                            it.source.playerOrException.sendMessage(TextComponent("$r"), Util.NIL_UUID)
                            1
                        } catch (e: Exception) {
                            throw e
                        }
                    })).then(literal("kd").executes {
                        try {
                            val r = ykd
                            it.source.playerOrException.sendMessage(TextComponent("$r"), Util.NIL_UUID)
                            1
                        } catch (e: Exception) {
                            throw e
                        }
                    }.then(argument("value", DoubleArgumentType.doubleArg()).executes {
                        try {
                            val r = DoubleArgumentType.getDouble(it, "value")
                            ykd = r
                            it.source.playerOrException.sendMessage(TextComponent("$r"), Util.NIL_UUID)
                            1
                        } catch (e: Exception) {
                            throw e
                        }
                    }))
                ).then(literal("z")
                    .then(literal("kp").executes {
                        try {
                            val r = zkp
                            it.source.playerOrException.sendMessage(TextComponent("$r"), Util.NIL_UUID)
                            1
                        } catch (e: Exception) {
                            throw e
                        }
                    }.then(argument("value", DoubleArgumentType.doubleArg()).executes {
                        try {
                            val r = DoubleArgumentType.getDouble(it, "value")
                            zkp = r
                            it.source.playerOrException.sendMessage(TextComponent("$r"), Util.NIL_UUID)
                            1
                        } catch (e: Exception) {
                            throw e
                        }
                    })).then(literal("ki").executes {
                        try {
                            val r = zki
                            it.source.playerOrException.sendMessage(TextComponent("$r"), Util.NIL_UUID)
                            1
                        } catch (e: Exception) {
                            throw e
                        }
                    }.then(argument("value", DoubleArgumentType.doubleArg()).executes {
                        try {
                            val r = DoubleArgumentType.getDouble(it, "value")
                            zki = r
                            it.source.playerOrException.sendMessage(TextComponent("$r"), Util.NIL_UUID)
                            1
                        } catch (e: Exception) {
                            throw e
                        }
                    })).then(literal("kd").executes {
                        try {
                            val r = zkd
                            it.source.playerOrException.sendMessage(TextComponent("$r"), Util.NIL_UUID)
                            1
                        } catch (e: Exception) {
                            throw e
                        }
                    }.then(argument("value", DoubleArgumentType.doubleArg()).executes {
                        try {
                            val r = DoubleArgumentType.getDouble(it, "value")
                            zkd = r
                            it.source.playerOrException.sendMessage(TextComponent("$r"), Util.NIL_UUID)
                            1
                        } catch (e: Exception) {
                            throw e
                        }
                    }))
                )
        )


    }
}
