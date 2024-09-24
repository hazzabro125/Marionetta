package com.hazzabro124.marionetta.items.custom

import com.hazzabro124.marionetta.items.MarionettaItems
import com.hazzabro124.marionetta.blocks.custom.ProxyAnchorBlock
import com.hazzabro124.marionetta.blocks.custom.ProxyBlock
import com.hazzabro124.marionetta.blocks.entity.ProxyBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.TextComponent
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.UseOnContext

class LinkStickItem: Item(
    Properties().tab(MarionettaItems.TAB)
) {

    override fun useOn(context: UseOnContext): InteractionResult {
        if (context.level.isClientSide) return InteractionResult.sidedSuccess(true)

        val clickedBlock = context.level.getBlockState(context.clickedPos).block
        if (clickedBlock is ProxyAnchorBlock) {
            savePosition(context.itemInHand, context.clickedPos)
            context.player?.sendMessage(TextComponent("Read position as ${context.clickedPos}"), context.player!!.uuid)
        } else if (clickedBlock is ProxyBlock){
            val savedPos = loadPosition(context.itemInHand) ?: run {
                context.player?.sendMessage(TextComponent("No Position Saved!"), context.player!!.uuid)
                return InteractionResult.FAIL
            }
            val be = context.level.getBlockEntity(context.clickedPos)
            if (be is ProxyBlockEntity)
                be.linkAnchor(savedPos)
            else
                return InteractionResult.FAIL
        }
        return InteractionResult.SUCCESS
    }

    companion object {
        fun savePosition(stack: ItemStack, pos: BlockPos) {
            stack.orCreateTag.putLong("marionetta\$savedPos",pos.asLong())
        }

        fun loadPosition(stack: ItemStack): BlockPos? {
            val tag = stack.orCreateTag
            if (tag.contains("marionetta\$savedPos"))
                return BlockPos.of(tag.getLong("marionetta\$savedPos"))
            return null
        }
    }
}