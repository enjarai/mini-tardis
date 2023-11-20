package dev.enjarai.minitardis.component.screen.app;

import com.mojang.serialization.Codec;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.block.console.ConsoleScreenBlockEntity;
import dev.enjarai.minitardis.canvas.ModCanvasUtils;
import dev.enjarai.minitardis.component.Tardis;
import dev.enjarai.minitardis.component.TardisControl;
import eu.pb4.mapcanvas.api.core.CanvasColor;
import eu.pb4.mapcanvas.api.core.CanvasImage;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.api.font.DefaultFonts;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ClickType;
import net.minecraft.util.Identifier;

public class StatusApp implements ScreenApp {
    public static final Codec<StatusApp> CODEC = Codec.unit(StatusApp::new);
    public static final Identifier ID = MiniTardis.id("status");

    @Override
    public void draw(TardisControl controls, ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        var tardis = controls.getTardis();

        DefaultFonts.VANILLA.drawText(canvas, tardis.getState().getName().getString(), 4, 6, 8, CanvasColor.WHITE_HIGH);

        DefaultFonts.VANILLA.drawText(canvas, "tmpStab: " + tardis.getStability(), 3, 4 + 48, 8, CanvasColor.WHITE_HIGH);

        var random = blockEntity.drawRandom;
        var isSolid = tardis.getState().isSolid(tardis);
        var stutterOffsetStability = isSolid ? 0 : random.nextBetween(-1, 1);
        drawVerticalBar(canvas, tardis.getStability() * 480 / 10000 + stutterOffsetStability, 96, 16, ModCanvasUtils.VERTICAL_BAR_ORANGE, "STB");
        var stutterOffsetFuel = isSolid ? 0 : random.nextBetween(-1, 1);
        drawVerticalBar(canvas, tardis.getFuel() * 480 / 10000 + stutterOffsetFuel, 72, 16, ModCanvasUtils.VERTICAL_BAR_BLUE, "ART");
    }

    private void drawVerticalBar(DrawableCanvas canvas, int value, int x, int y, CanvasImage barType, String label) {
        CanvasUtils.draw(canvas, x, y, ModCanvasUtils.VERTICAL_BAR_EMPTY);
        for (int ly = 48 - value; ly < 48; ly++) {
            for (int lx = 0; lx < 16; lx++) {
                byte color = barType.getRaw(8 + lx, 8 + ly);

                if (color != 0) {
                    canvas.setRaw(8 + lx + x, 8 + ly + y, color);
                }
            }
        }

        var labelWidth = DefaultFonts.VANILLA.getTextWidth(label, 8);
        DefaultFonts.VANILLA.drawText(canvas, label, x + 15 - labelWidth / 2, y + 64, 8, CanvasColor.WHITE_HIGH);
    }

    @Override
    public void drawBackground(TardisControl control, ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        CanvasUtils.draw(canvas, 0, 0, ModCanvasUtils.STATUS_BACKGROUND);
    }

    @Override
    public boolean onClick(TardisControl controls, ConsoleScreenBlockEntity blockEntity, ServerPlayerEntity player, ClickType type, int x, int y) {
        return false;
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
