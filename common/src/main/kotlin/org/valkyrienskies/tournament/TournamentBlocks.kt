package org.valkyrienskies.tournament

import net.minecraft.core.BlockPos
import net.minecraft.core.Registry
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.minecraft.world.level.Explosion
import net.minecraft.world.level.block.*
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.Material
import org.valkyrienskies.mod.common.hooks.VSGameEvents
import org.valkyrienskies.tournament.util.extension.explodeShip
import org.valkyrienskies.tournament.blocks.*
import org.valkyrienskies.tournament.registry.DeferredRegister
import org.valkyrienskies.tournament.registry.RegistrySupplier

@Suppress("unused")
object TournamentBlocks {
    private val BLOCKS = DeferredRegister.create(TournamentMod.MOD_ID, Registry.BLOCK_REGISTRY)
    private val ITEMS = ArrayList<Pair<String, ()->Item>>()


    lateinit var FLOATER                  : RegistrySupplier<Block>
    lateinit var THRUSTER                 : RegistrySupplier<ThrusterBlock>
    lateinit var THRUSTER_TINY            : RegistrySupplier<ThrusterBlock>

    // lateinit var EXPLOSIVE_TEST           : RegistrySupplier<TestExplosiveBlock>

    fun register() {
        FLOATER                  = register("floater") { Block(
            BlockBehaviour.Properties.of(Material.WOOD)
                .sound(SoundType.WOOD)
                .strength(1.0f, 2.0f)
        )}
        THRUSTER                 = register("thruster") {
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
        THRUSTER_TINY            = register("tiny_thruster") {
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






        // EXPLOSIVE_TEST           = register("explosive_test", ::TestExplosiveBlock)

        /*
        register("ore_phynite") {
            OreBlock(BlockBehaviour.Properties.of(TournamentMaterials.PHYNITE)
                .strength(3.0f, 3.0f)
            )
        }
         */


        // old:

        BLOCKS.applyAll()
        VSGameEvents.registriesCompleted.on { _, _ ->
            makeFlammables()
        }
    }

    private fun <T: Block> register(name: String, block: () -> T): RegistrySupplier<T> {
        val supplier = BLOCKS.register(name, block)
        ITEMS.add(name to { BlockItem(supplier.get(), Item.Properties().tab(TournamentItems.TAB)) })
        return supplier
    }

    private fun <T: Block> register(name: String, tab: CreativeModeTab?, block: () -> T): RegistrySupplier<T> {
        val supplier = BLOCKS.register(name, block)
        tab?.let {
            ITEMS.add(name to { BlockItem(supplier.get(), Item.Properties().tab(tab)) })
        }
        return supplier
    }

    private fun <T: Block> register(name: String, block: () -> T, item: () -> Item): RegistrySupplier<T> {
        val supplier = BLOCKS.register(name, block)
        ITEMS.add(name to item)
        return supplier
    }

    fun flammableBlock(block: Block, flameOdds: Int, burnOdds: Int) {
        val fire = Blocks.FIRE as FireBlock
        fire.setFlammable(block, flameOdds, burnOdds)
    }

    fun makeFlammables() {

        flammableBlock(FLOATER.get(), 30, 60)
    }

    fun registerItems(items: DeferredRegister<Item>) {
        ITEMS.forEach { items.register(it.first, it.second) }
    }

}
