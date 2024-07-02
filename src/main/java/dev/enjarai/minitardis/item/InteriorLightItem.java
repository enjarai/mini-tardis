package dev.enjarai.minitardis.item;

import net.minecraft.block.Block;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

import static dev.enjarai.minitardis.block.InteriorLightBlock.ORDER;

public class InteriorLightItem extends TooltipPolymerBlockItem {
    public InteriorLightItem(Block block, Settings settings, Item virtualItem) {
        super(block, settings, virtualItem);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType options) {
        var stateComponent = stack.get(DataComponentTypes.BLOCK_STATE);
        if (stateComponent != null) {
            var order = stateComponent.getValue(ORDER);
            tooltip.add(Text.translatable("block.mini_tardis.interior_light.tooltip.order", order).fillStyle(Style.EMPTY.withColor(Formatting.GRAY)));
            tooltip.add(Text.empty());
        }
        super.appendTooltip(stack, context, tooltip,  options);
    }
}
