package org.valkyrienskies.tournament

import net.minecraft.core.Registry
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.*
import org.valkyrienskies.tournament.blocks.*
import org.valkyrienskies.tournament.registry.DeferredRegister
import org.valkyrienskies.tournament.registry.RegistrySupplier

@Suppress("unused")
object TournamentBlocks {
    private val BLOCKS = DeferredRegister.create(TournamentMod.MOD_ID, Registry.BLOCK_REGISTRY)
    private val ITEMS = ArrayList<Pair<String, ()->Item>>()


    lateinit var SHIP_ASSEMBLER           : RegistrySupplier<ShipAssemblerBlock>
    lateinit var FLOATER                  : RegistrySupplier<Block>
    lateinit var THRUSTER                 : RegistrySupplier<ThrusterBlock>
    lateinit var THRUSTER_TINY            : RegistrySupplier<ThrusterBlock>

    fun register() {
        SHIP_ASSEMBLER = register("ship_assembler", ::ShipAssemblerBlock)
        THRUSTER = register("thruster") {
            ThrusterBlock(
                { 1.0 },
                ParticleTypes.CAMPFIRE_SIGNAL_SMOKE
            ) {
                val t = TournamentConfig.SERVER.thrusterTiersNormal
                if (t !in 1..5) {
                    throw IllegalStateException("Thruster tier must be in range 1..5")
                }
                t
            }
        }
        THRUSTER_TINY = register("tiny_thruster") {
            ThrusterBlock(
                { TournamentConfig.SERVER.thrusterTinyForceMultiplier },
                ParticleTypes.CAMPFIRE_COSY_SMOKE
            ) {
                val t = TournamentConfig.SERVER.thrusterTiersTiny
                if (t !in 1..5) {
                    throw IllegalStateException("Thruster tier must be in range 1..5")
                }
                t
            }
        }



        BLOCKS.applyAll()
    }

    private fun <T: Block> register(name: String, block: () -> T): RegistrySupplier<T> {
        val supplier = BLOCKS.register(name, block)
        ITEMS.add(name to { BlockItem(supplier.get(), Item.Properties().tab(TournamentItems.TAB)) })
        return supplier
    }

    fun registerItems(items: DeferredRegister<Item>) {
        ITEMS.forEach { items.register(it.first, it.second) }
    }

}
