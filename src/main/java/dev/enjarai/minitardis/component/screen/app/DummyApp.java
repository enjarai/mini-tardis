package dev.enjarai.minitardis.component.screen.app;

import com.mojang.serialization.Codec;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.block.console.ConsoleScreenBlockEntity;
import dev.enjarai.minitardis.canvas.ModCanvasUtils;
import dev.enjarai.minitardis.component.TardisControl;
import eu.pb4.mapcanvas.api.core.CanvasColor;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ClickType;
import net.minecraft.util.Identifier;

public class DummyApp implements ScreenApp {
    public static final Codec<DummyApp> CODEC = Codec.unit(DummyApp::new);
    public static final Identifier ID = MiniTardis.id("dummy");

    @Override
    public AppView getView(TardisControl controls) {
        return new AppView() {
            @Override
            public void draw(ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas) {
                ModCanvasUtils.drawCenteredText(canvas, "This app does not exist anymore.", 64, 40, CanvasColor.WHITE_HIGH);
            }

            @Override
            public boolean onClick(ConsoleScreenBlockEntity blockEntity, ServerPlayerEntity player, ClickType type, int x, int y) {
                return false;
            }
        };
    }

    @Override
    public void drawIcon(TardisControl controls, ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        CanvasUtils.draw(canvas, 0, 0, ModCanvasUtils.DUMMY_APP);
    }

    @Override
    public Identifier id() {
        return ID;
    }
}
