package dev.enjarai.minitardis.item;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class TooltipBlockItem extends BlockItem {
    public TooltipBlockItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable(getTranslationKey(stack) + ".tooltip").fillStyle(Style.EMPTY.withColor(Formatting.GRAY)));

        super.appendTooltip(stack, context, tooltip, type);
    }
}
