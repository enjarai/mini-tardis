package dev.enjarai.minitardis.component.screen.app;

import com.mojang.serialization.Codec;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.block.console.ConsoleScreenBlockEntity;
import dev.enjarai.minitardis.canvas.ModCanvasUtils;
import dev.enjarai.minitardis.component.TardisControl;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import net.minecraft.util.Identifier;

public class SnakeOnBadAppleApp extends SnakeApp {
    public static final Codec<SnakeOnBadAppleApp> CODEC = Codec.unit(SnakeOnBadAppleApp::new);
    public static final Identifier ID = MiniTardis.id("bad_snake");

    @Override
    public AppView getView(TardisControl controls) {
        return new BadSnakeAppView(controls);
    }

    @Override
    public void drawIcon(TardisControl controls, ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        CanvasUtils.draw(canvas, 0, 0, ModCanvasUtils.BAD_SNAKE_APP);
    }

    @Override
    public Identifier id() {
        return ID;
    }

    public static class BadSnakeAppView extends SnakeApp.SnakeAppView {
        public BadSnakeAppView(TardisControl tardisControl) {
            super(tardisControl);
        }
        public BadAppleApp.BadAppleView badAppleAppView = new BadAppleApp.BadAppleView() {
            @Override
            public void endAnimation(ConsoleScreenBlockEntity blockEntity) {
                BadSnakeAppView.this.reset(blockEntity);
            }
        };
        public boolean animationStarted = false;

        @Override
        public void draw(ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas) {
            if(animationStarted) {
                badAppleAppView.draw(blockEntity, canvas);
            }
            CanvasUtils.draw(canvas, 0, 0, ModCanvasUtils.SNAKE_OVERLAY);
            super.draw(blockEntity, canvas);
        }

        @Override
        public void ateApple(ConsoleScreenBlockEntity blockEntity) {
            if(!animationStarted) badAppleAppView.screenOpen(blockEntity);
            animationStarted = true;
            super.ateApple(blockEntity);
        }

        public void reset(ConsoleScreenBlockEntity blockEntity) {
            badAppleAppView.screenClose(blockEntity);
            badAppleAppView.screenOpen(blockEntity);
        }

        @Override
        public void screenClose(ConsoleScreenBlockEntity blockEntity) {
            badAppleAppView.screenClose(blockEntity);
        }
    }

}
