package dev.enjarai.minitardis.item;

import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.component.screen.app.DimensionsApp;
import dev.enjarai.minitardis.component.screen.app.ScreenApp;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FloppyItem extends Item implements PolymerItem {
    public static final PolymerModelData MODEL = PolymerResourcePackUtils.requestModel(Items.IRON_INGOT, MiniTardis.id("item/floppy"));

    public FloppyItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        var stack = user.getStackInHand(hand);
        var apps = getApps(stack);
        for (int i = 0; i < apps.size(); i++) {
            var app = apps.get(i);
            if (app instanceof DimensionsApp dimensionsApp
                    && dimensionsApp.canAddAsAccessible(world.getRegistryKey())
                    && !dimensionsApp.accessibleDimensions.contains(world.getRegistryKey())) {
                removeApp(stack, i);
                dimensionsApp.accessibleDimensions.add(world.getRegistryKey());
                addApp(stack, dimensionsApp);
                return TypedActionResult.success(stack);
            }
        }
        return super.use(world, user, hand);
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType options) {
        for (var app : getApps(stack)) {
            tooltip.addAll(app.getName().getWithStyle(Style.EMPTY.withColor(Formatting.GRAY)));
            app.appendTooltip(tooltip);
        }
    }

    public static List<ScreenApp> getApps(ItemStack stack) {
        if (stack.isEmpty()) return List.of();
        if(stack.contains(ModDataComponents.SCREEN_APPS_COMPONENT_TYPE)) {
            return stack.get(ModDataComponents.SCREEN_APPS_COMPONENT_TYPE).screenApps();
        } else {
            return List.of();
        }
        /*
        if (nbt.contains("apps", NbtElement.LIST_TYPE)) {
            var list = ImmutableList.<ScreenApp>builder();
            for (var appElement : nbt.getList("apps", NbtElement.COMPOUND_TYPE)) {
                ScreenApp.CODEC.decode(NbtOps.INSTANCE, appElement).result().ifPresent(app -> list.add(app.getFirst()));
            }
            return list.build();
        } else {
            return List.of();
        }

         */
    }

    public static void addApp(ItemStack stack, ScreenApp app) {

        List<ScreenApp> appsList;
        if (stack.contains(ModDataComponents.SCREEN_APPS_COMPONENT_TYPE)) {
            appsList = new ArrayList<>(stack.get(ModDataComponents.SCREEN_APPS_COMPONENT_TYPE).screenApps());
        } else {
            appsList = new ArrayList<>();
        }
        appsList.add(app);
        stack.set(ModDataComponents.SCREEN_APPS_COMPONENT_TYPE, new ScreenApps(appsList.stream().toList()));

        /*
        var nbt = stack.getOrCreateNbt();

        NbtList appsList;
        if (!nbt.contains("apps", NbtElement.COMPOUND_TYPE)) {
            appsList = nbt.getList("apps", NbtElement.COMPOUND_TYPE);
        } else {
            appsList = new NbtList();
        }

        ScreenApp.CODEC.encodeStart(NbtOps.INSTANCE, app).result().ifPresent(appsList::add);

        nbt.put("apps", appsList);

         */
    }

    public static boolean removeApp(ItemStack stack, int index) {

        List<ScreenApp> appsList;
        if (stack.contains(ModDataComponents.SCREEN_APPS_COMPONENT_TYPE)) {
            appsList = new ArrayList<>(stack.get(ModDataComponents.SCREEN_APPS_COMPONENT_TYPE).screenApps());
        } else {
            appsList = new ArrayList<>();
        }

        if (appsList.size() > index) {
            appsList.remove(index);
            stack.set(ModDataComponents.SCREEN_APPS_COMPONENT_TYPE, new ScreenApps(appsList.stream().toList()));
            return true;
        }
        return false;
        /*
        var nbt = stack.getOrCreateNbt();

        NbtList appsList;
        if (!nbt.contains("apps", NbtElement.COMPOUND_TYPE)) {
            appsList = nbt.getList("apps", NbtElement.COMPOUND_TYPE);
        } else {
            appsList = new NbtList();
        }

        if (appsList.size() > index) {
            appsList.remove(index);
            nbt.put("apps", appsList);
            return true;
        }

        return false;

         */
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return MODEL.item();
    }

    @Override
    public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return MODEL.value();
    }
}
