package dev.enjarai.minitardis.component.screen.app;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.enjarai.minitardis.block.console.ScreenBlockEntity;
import dev.enjarai.minitardis.canvas.TardisCanvasUtils;
import dev.enjarai.minitardis.component.TardisControl;
import dev.enjarai.minitardis.component.TardisLocation;
import dev.enjarai.minitardis.component.screen.element.SmallButtonElement;
import dev.enjarai.minitardis.component.screen.element.WaypointListElement;
import dev.enjarai.minitardis.data.ModDataStuff;
import dev.enjarai.minitardis.data.RandomAppLootFunction;
import eu.pb4.mapcanvas.api.core.CanvasColor;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.api.font.DefaultFonts;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class WaypointsApp implements ScreenApp {
    public static final MapCodec<WaypointsApp> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING.xmap(Integer::parseInt, Object::toString), TardisLocation.CODEC).optionalFieldOf("waypoints", Map.of()).forGetter(app -> app.waypoints)
    ).apply(instance, WaypointsApp::new));

    private final Map<Integer, TardisLocation> waypoints;

    private WaypointsApp(Map<Integer, TardisLocation> waypoints) {
        this.waypoints = new HashMap<>(waypoints);
    }

    public WaypointsApp() {
        this.waypoints = new HashMap<>();
    }

    @Override
    public AppView getView(TardisControl controls) {
        return new ElementHoldingView(controls) {
            final WaypointListElement waypointList = addElement(new WaypointListElement(4, 45, 124, 50, waypoints));
            final SmallButtonElement storeLocation = addElement(new SmallButtonElement(2, 2, "Save",
                    controls1 -> controls1.getTardis().getDestination().ifPresent(location -> {
                        if (waypointList.selected != null) {
                            waypointList.selected.setWaypointValue(location);
                        }
                    })));
            final SmallButtonElement loadLocation = addElement(new SmallButtonElement(2, 2, "Load",
                    controls1 -> {
                        if (waypointList.selected != null && waypointList.selected.getWaypointValue() != null) {
                            controls1.getTardis().setDestination(waypointList.selected.getWaypointValue(), false);
                        }
                    }));
            final SmallButtonElement deleteLocation = addElement(new SmallButtonElement(30, 2, "Clear",
                    controls1 -> {
                        if (waypointList.selected != null) {
                            waypointList.selected.clearWaypointValue();
                        }
                    }));

            @Override
            public void draw(ScreenBlockEntity blockEntity, DrawableCanvas canvas) {
                storeLocation.visible = false;
                loadLocation.visible = false;
                deleteLocation.visible = false;

                if (waypointList.selected == null) {
                    DefaultFonts.VANILLA.drawText(canvas, "Select Waypoint below", 6, 22 + 9, 8, CanvasColor.LIGHT_GRAY_HIGH);
                } else {
                    var location = waypointList.selected.getWaypointValue();
                    if (location == null) {
                        storeLocation.visible = true;

                        DefaultFonts.VANILLA.drawText(canvas, "Empty", 6, 22 + 9, 8, CanvasColor.LIGHT_GRAY_HIGH);
                    } else {
                        loadLocation.visible = true;
                        deleteLocation.visible = true;

                        if (location.equals(controls.getTardis().getDestination().orElse(null))) {
                            CanvasUtils.draw(canvas, 2, 18, TardisCanvasUtils.getSprite("history_current_outline"));
                        }
                        GpsApp.drawLocation(Optional.of(location), canvas, 6, 22);
                    }
                }
                super.draw(blockEntity, canvas);
            }

            @Override
            public void drawBackground(ScreenBlockEntity blockEntity, DrawableCanvas canvas) {
                CanvasUtils.draw(canvas, 0, 0, TardisCanvasUtils.getSprite("waypoints_background"));
            }
        };
    }

    @Override
    public void drawIcon(TardisControl controls, ScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        CanvasUtils.draw(canvas, 0, 0, TardisCanvasUtils.getSprite("app/waypoints"));
    }

    @Override
    public void appendTooltip(List<Text> tooltip) {
        tooltip.add(Text.literal(" ").append(Text.translatable("mini_tardis.app.mini_tardis.waypoints.tooltip", waypoints.size()))
                .fillStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY)));
    }

    @Override
    public void applyLootModifications(LootContext context, RandomAppLootFunction lootFunction) {
        Vec3d vec3d = context.get(LootContextParameters.ORIGIN);

        if (vec3d != null) {
            ServerWorld serverWorld = context.getWorld();
            Random random = context.getRandom();

            for (int i = 0; i < random.nextBetween(1, 8); i++) {
                BlockPos blockPos = serverWorld.locateStructure(ModDataStuff.WAYPOINT_APP_RANDOMLY_FOUND_STRUCTURES,
                        BlockPos.ofFloored(vec3d), 50, true);

                if (blockPos != null) {
                    waypoints.put(random.nextInt((124 / 8) * (50 / 8)),
                            new TardisLocation(serverWorld.getRegistryKey(), blockPos.withY(64),
                                    Direction.fromHorizontal(random.nextInt())));
                }
            }
        }
    }

    @Override
    public ScreenAppType<?> getType() {
        return ScreenAppTypes.WAYPOINTS;
    }
}
