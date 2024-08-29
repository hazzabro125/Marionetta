package com.hazzabro124.marionetta.items

import com.hazzabro124.marionetta.MarionettaItems
import com.hazzabro124.marionetta.blocks.ProxyAnchor
import com.hazzabro124.marionetta.blocks.ProxyBlock
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.TextComponent
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.Item
import net.minecraft.world.item.context.UseOnContext
import org.valkyrienskies.mod.common.util.toJOML

class LinkStick: Item(
    Properties().tab(MarionettaItems.TAB)
) {

    var savedPos: BlockPos? = null

    override fun useOn(context: UseOnContext): InteractionResult {
        val clickedBlock = context.level.getBlockState(context.clickedPos).block
        if (clickedBlock is ProxyAnchor) {
            savedPos = context.clickedPos
            context.player?.sendMessage(TextComponent("Read position as ${savedPos}"), context.player!!.uuid)
        } else if (clickedBlock is ProxyBlock){
            clickedBlock.linkedAnchor = savedPos
            context.player?.sendMessage(TextComponent("Wrote position as $savedPos"), context.player!!.uuid)
        }
        return InteractionResult.SUCCESS
    }
}