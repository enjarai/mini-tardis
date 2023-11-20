package dev.enjarai.minitardis.component.screen.app;

import com.mojang.serialization.Codec;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.block.console.ConsoleScreenBlockEntity;
import dev.enjarai.minitardis.canvas.ModCanvasUtils;
import dev.enjarai.minitardis.component.TardisControl;
import eu.pb4.mapcanvas.api.core.CanvasColor;
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
        DefaultFonts.VANILLA.drawText(canvas, controls.getTardis().getState().getName().getString(), 4, 6, 8, CanvasColor.WHITE_HIGH);

        DefaultFonts.VANILLA.drawText(canvas, "tmpStab: " + controls.getTardis().getStability(), 3, 4 + 48, 8, CanvasColor.WHITE_HIGH);
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
