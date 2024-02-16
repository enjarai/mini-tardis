package dev.enjarai.minitardis.item;

import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static dev.enjarai.minitardis.block.InteriorLightBlock.ORDER;

public class InteriorLightItem extends TooltipPolymerBlockItem {
    public InteriorLightItem(Block block, Settings settings, Item virtualItem) {
        super(block, settings, virtualItem);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        var stateNbt = stack.getSubNbt("BlockStateTag");
        if (stateNbt != null && stateNbt.contains(ORDER.getName(), NbtElement.STRING_TYPE)) {
            var order = stateNbt.getString(ORDER.getName());
            tooltip.add(Text.translatable("block.mini_tardis.interior_light.tooltip.order", order).fillStyle(Style.EMPTY.withColor(Formatting.GRAY)));
            tooltip.add(Text.empty());
        }

        super.appendTooltip(stack, world, tooltip, context);
    }
}
