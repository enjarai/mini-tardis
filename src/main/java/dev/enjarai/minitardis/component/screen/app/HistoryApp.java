package dev.enjarai.minitardis.component.screen.app;

import com.mojang.serialization.Codec;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.block.console.ConsoleScreenBlockEntity;
import dev.enjarai.minitardis.canvas.ModCanvasUtils;
import dev.enjarai.minitardis.component.TardisControl;
import dev.enjarai.minitardis.component.TardisLocation;
import eu.pb4.mapcanvas.api.core.CanvasColor;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.api.font.DefaultFonts;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ClickType;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class HistoryApp extends ElementHoldingApp {
    public static final Codec<HistoryApp> CODEC = Codec.unit(HistoryApp::new);
    public static final Identifier ID = MiniTardis.id("history");

    @Override
    public void draw(TardisControl controls, ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        var current = controls.getTardis().getCurrentLocation();
        DefaultFonts.VANILLA.drawText(canvas, "Current Location", 3, 4, 8, CanvasColor.WHITE_HIGH);
        drawLocation(current, canvas, 3, 4 + 20);

        var destination = controls.getTardis().getDestination();
        DefaultFonts.VANILLA.drawText(canvas, "Destination", 3, 4 + 41, 8, CanvasColor.WHITE_HIGH);
        drawLocation(destination, canvas, 3, 4 + 61);

        var isLocked = controls.isDestinationLocked();
        var color = isLocked ? CanvasColor.LIME_HIGH : CanvasColor.RED_HIGH;
//        CanvasUtils.fill(canvas, 2, 84, 126, 94, color);
        var lockedText = isLocked ? ">> Locked <<" : "|| Unlocked ||";
        var lockedWidth = DefaultFonts.VANILLA.getTextWidth(lockedText, 8);
        DefaultFonts.VANILLA.drawText(canvas, lockedText, 64 - lockedWidth / 2, 86, 8, color);

        super.draw(controls, blockEntity, canvas);
    }

    @Override
    public void drawBackground(TardisControl control, ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        CanvasUtils.draw(canvas, 0, 0, ModCanvasUtils.HISTORY_BACKGROUND);
    }

    @Override
    public void drawIcon(TardisControl controls, ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        CanvasUtils.draw(canvas, 0, 0, ModCanvasUtils.GPS_APP);
    }

    @Override
    public Identifier id() {
        return ID;
    }
}
