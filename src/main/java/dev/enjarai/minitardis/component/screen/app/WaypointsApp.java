package dev.enjarai.minitardis.component.screen.app;

import com.mojang.serialization.Codec;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.block.console.ConsoleScreenBlockEntity;
import dev.enjarai.minitardis.canvas.ModCanvasUtils;
import dev.enjarai.minitardis.component.TardisControl;
import dev.enjarai.minitardis.component.flight.RefuelingState;
import eu.pb4.mapcanvas.api.core.CanvasColor;
import eu.pb4.mapcanvas.api.core.CanvasImage;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.api.font.DefaultFonts;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ClickType;
import net.minecraft.util.Identifier;

public class WaypointsApp implements ScreenApp {
    public static final Codec<WaypointsApp> CODEC = Codec.unit(WaypointsApp::new);
    public static final Identifier ID = MiniTardis.id("waypoints");

    @Override
    public AppView getView(TardisControl controls) {
        return new ElementHoldingView(controls) {
            @Override
            public void draw(ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas) {
                // TODO
                super.draw(blockEntity, canvas);
            }

            @Override
            public void drawBackground(ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas) {
                CanvasUtils.draw(canvas, 0, 0, ModCanvasUtils.STATUS_BACKGROUND);
            }
        };
    }

    @Override
    public void drawIcon(TardisControl controls, ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        CanvasUtils.draw(canvas, 0, 0, ModCanvasUtils.STATUS_APP);
    }

    @Override
    public Identifier id() {
        return ID;
    }
}
