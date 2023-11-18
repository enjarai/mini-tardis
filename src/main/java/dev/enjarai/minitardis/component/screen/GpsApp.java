package dev.enjarai.minitardis.component.screen;

import com.mojang.serialization.Codec;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.component.TardisControl;
import eu.pb4.mapcanvas.api.core.CanvasColor;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.api.font.DefaultFonts;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ClickType;
import net.minecraft.util.Identifier;

public class GpsApp implements ScreenApp {
    public static final Codec<GpsApp> CODEC = Codec.unit(GpsApp::new);
    public static final Identifier ID = MiniTardis.id("gps");

    @Override
    public void draw(TardisControl controls, DrawableCanvas canvas) {
        var destination = controls.getTardis().getDestination();
        DefaultFonts.VANILLA.drawText(canvas,
                "X: " + destination.map(l -> String.valueOf(l.pos().getX())).orElse("-"),
                3, 16 + 3, 8, CanvasColor.WHITE_HIGH);
    }

    @Override
    public boolean onClick(TardisControl controls, ServerPlayerEntity player, ClickType type, int x, int y) {
        return false;
    }

    @Override
    public void drawIcon(TardisControl controls, DrawableCanvas canvas) {
        CanvasUtils.fill(canvas, 0, 0, 24, 24, CanvasColor.BRIGHT_RED_HIGH);
    }

    @Override
    public Identifier id() {
        return ID;
    }
}
