package dev.enjarai.minitardis.component.screen.app;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.enjarai.minitardis.block.console.ScreenBlockEntity;
import dev.enjarai.minitardis.canvas.TardisCanvasUtils;
import dev.enjarai.minitardis.component.TardisControl;
import eu.pb4.mapcanvas.api.core.CanvasColor;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ClickType;

public class DummyApp implements ScreenApp {
    public static final MapCodec<DummyApp> CODEC = MapCodec.unit(DummyApp::new);

    @Override
    public AppView getView(TardisControl controls) {
        return new AppView() {
            @Override
            public void draw(ScreenBlockEntity blockEntity, DrawableCanvas canvas) {
                TardisCanvasUtils.drawCenteredText(canvas, "This app does not exist anymore.", 64, 40, CanvasColor.WHITE_HIGH);
            }

            @Override
            public boolean onClick(ScreenBlockEntity blockEntity, ServerPlayerEntity player, ClickType type, int x, int y) {
                return false;
            }
        };
    }

    @Override
    public void drawIcon(TardisControl controls, ScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        CanvasUtils.draw(canvas, 0, 0, TardisCanvasUtils.getSprite("app/dummy"));
    }

    @Override
    public ScreenAppType<?> getType() {
        return ScreenAppTypes.DUMMY;
    }
}
