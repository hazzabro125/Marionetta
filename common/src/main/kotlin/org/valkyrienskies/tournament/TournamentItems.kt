package org.valkyrienskies.tournament

import net.minecraft.core.Registry
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import org.valkyrienskies.tournament.registry.DeferredRegister
import org.valkyrienskies.tournament.registry.RegistrySupplier
import java.util.logging.Logger


@Suppress("unused")
object TournamentItems {
    private val ITEMS = DeferredRegister.create(TournamentMod.MOD_ID, Registry.ITEM_REGISTRY)

    lateinit var UPGRADE_THRUSTER  :  RegistrySupplier<Item>

    lateinit var TAB: CreativeModeTab

    fun register() {
        UPGRADE_THRUSTER        = ITEMS.register("upgrade_thruster") {
            Item(Item.Properties().stacksTo(16).tab(TAB))
        }
        ITEMS.register("iron_cube") {
            Item(Item.Properties().stacksTo(64).tab(TAB))
        }
        ITEMS.register("coal_dust") {
            Item(Item.Properties().stacksTo(64).tab(TAB))
        }

        // old:

        TournamentBlocks.registerItems(ITEMS)
        ITEMS.applyAll()
    }

}