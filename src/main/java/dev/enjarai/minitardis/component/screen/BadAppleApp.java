package dev.enjarai.minitardis.component.screen;

import com.mojang.serialization.Codec;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.canvas.BadApple;
import dev.enjarai.minitardis.component.TardisControl;
import eu.pb4.mapcanvas.api.core.CanvasColor;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.api.font.DefaultFonts;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ClickType;
import net.minecraft.util.Identifier;

public class BadAppleApp implements ScreenApp {
    public static final Codec<BadAppleApp> CODEC = Codec.unit(BadAppleApp::new);
    public static final Identifier ID = MiniTardis.id("bad_apple");

    private int frameCounter = 0;

    @Override
    public void draw(TardisControl controls, DrawableCanvas canvas) {
        for (int i = 0; i < 96 * 96; i++) {
            var pixel = BadApple.getPixel(frameCounter, i);
            var color = switch (pixel) {
                case 0 -> CanvasColor.BLACK_LOWEST;
                case 1 -> CanvasColor.BLACK_LOW;
                case 2 -> CanvasColor.BLACK_NORMAL;
                case 3 -> CanvasColor.BLACK_HIGH;
                case 4 -> CanvasColor.WHITE_LOWEST;
                case 5 -> CanvasColor.WHITE_LOW;
                case 6 -> CanvasColor.WHITE_NORMAL;
                case 7 -> CanvasColor.WHITE_HIGH;
                default -> CanvasColor.WHITE_HIGH;
            };
            canvas.set(i % 128, i / 128, color);
        }

        frameCounter++;
    }

    @Override
    public boolean onClick(TardisControl controls, ServerPlayerEntity player, ClickType type, int x, int y) {
        return false;
    }

    @Override
    public void drawIcon(TardisControl controls, DrawableCanvas canvas) {
        CanvasUtils.fill(canvas, 0, 0, 24, 24, CanvasColor.BLACK_HIGH);
    }

    @Override
    public Identifier id() {
        return ID;
    }
}
