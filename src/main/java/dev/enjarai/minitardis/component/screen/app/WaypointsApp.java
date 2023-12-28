package dev.enjarai.minitardis.component.screen.app;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.block.console.ConsoleScreenBlockEntity;
import dev.enjarai.minitardis.canvas.ModCanvasUtils;
import dev.enjarai.minitardis.component.TardisControl;
import dev.enjarai.minitardis.component.TardisLocation;
import dev.enjarai.minitardis.component.screen.element.SmallButtonElement;
import dev.enjarai.minitardis.component.screen.element.WaypointListElement;
import eu.pb4.mapcanvas.api.core.CanvasColor;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.api.font.DefaultFonts;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class WaypointsApp implements ScreenApp {
    public static final Codec<WaypointsApp> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING.xmap(Integer::parseInt, Object::toString), TardisLocation.CODEC).optionalFieldOf("waypoints", Map.of()).forGetter(app -> app.waypoints)
    ).apply(instance, WaypointsApp::new));
    public static final Identifier ID = MiniTardis.id("waypoints");

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
            public void draw(ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas) {
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
                            CanvasUtils.draw(canvas, 2, 18, ModCanvasUtils.HISTORY_CURRENT_OUTLINE);
                        }
                        GpsApp.drawLocation(Optional.of(location), canvas, 6, 22);
                    }
                }
                super.draw(blockEntity, canvas);
            }

            @Override
            public void drawBackground(ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas) {
                CanvasUtils.draw(canvas, 0, 0, ModCanvasUtils.WAYPOINTS_BACKGROUND);
            }
        };
    }

    @Override
    public void drawIcon(TardisControl controls, ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        CanvasUtils.draw(canvas, 0, 0, ModCanvasUtils.WAYPOINTS_APP);
    }

    @Override
    public Identifier id() {
        return ID;
    }
}
