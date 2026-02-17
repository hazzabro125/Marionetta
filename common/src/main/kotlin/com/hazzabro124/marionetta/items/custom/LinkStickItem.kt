package com.hazzabro124.marionetta.items.custom

import com.hazzabro124.marionetta.items.MarionettaItems
import com.hazzabro124.marionetta.blocks.custom.ProxyAnchorBlock
import com.hazzabro124.marionetta.blocks.custom.ProxyBlock
import com.hazzabro124.marionetta.blocks.entity.ProxyBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.UseOnContext

class LinkStickItem: Item(
    Properties()
) {

    override fun useOn(context: UseOnContext): InteractionResult {
        if (context.level.isClientSide) return InteractionResult.sidedSuccess(true)

        val clickedBlock = context.level.getBlockState(context.clickedPos).block
        if (clickedBlock is ProxyAnchorBlock) {
            savePosition(context.itemInHand, context.clickedPos)
            context.player?.sendSystemMessage(Component.literal("Read position as ${context.clickedPos}"))
        } else if (clickedBlock is ProxyBlock){
            val savedPos = loadPosition(context.itemInHand) ?: run {
                context.player?.sendSystemMessage(Component.literal("No Position Saved!"))
                return InteractionResult.FAIL
            }
            context.player?.sendSystemMessage(Component.literal("Loading position as ${savedPos}"))
            val be = context.level.getBlockEntity(context.clickedPos)
            if (be is ProxyBlockEntity) {
                be.linkAnchor(savedPos)
                context.player?.sendSystemMessage(Component.literal("Link Successful!"))
            }else
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