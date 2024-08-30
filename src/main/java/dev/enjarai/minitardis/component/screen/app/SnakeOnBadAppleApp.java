package dev.enjarai.minitardis.component.screen.app;

import com.mojang.serialization.Codec;
import dev.enjarai.minitardis.block.console.ScreenBlockEntity;
import dev.enjarai.minitardis.canvas.TardisCanvasUtils;
import dev.enjarai.minitardis.component.TardisControl;
import dev.enjarai.minitardis.component.screen.canvas.patbox.DrawableCanvas;

public class SnakeOnBadAppleApp extends SnakeApp {
    public static final Codec<SnakeOnBadAppleApp> CODEC = Codec.unit(SnakeOnBadAppleApp::new);

    @Override
    public AppView getView(TardisControl controls) {
        return new BadSnakeAppView(controls);
    }

    @Override
    public void drawIcon(TardisControl controls, ScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        canvas.draw(0, 0, TardisCanvasUtils.getSprite("app/bad_snake"));
    }

    @Override
    public ScreenAppType<?> getType() {
        return null; // TODO?
    }

    public static class BadSnakeAppView extends SnakeApp.SnakeAppView {
        public BadSnakeAppView(TardisControl tardisControl) {
            super(tardisControl);
        }
        public BadAppleApp.BadAppleView badAppleAppView = new BadAppleApp.BadAppleView() {
            @Override
            public void endAnimation(ScreenBlockEntity blockEntity) {
                BadSnakeAppView.this.reset(blockEntity);
            }
        };
        public boolean animationStarted = false;

        @Override
        public void draw(ScreenBlockEntity blockEntity, DrawableCanvas canvas) {
            if(animationStarted) {
                badAppleAppView.draw(blockEntity, canvas);
            }
            canvas.draw(0, 0, TardisCanvasUtils.getSprite("snake_overlay"));
            super.draw(blockEntity, canvas);
        }

        @Override
        public void ateApple(ScreenBlockEntity blockEntity) {
            if(!animationStarted) badAppleAppView.screenOpen(blockEntity);
            animationStarted = true;
            super.ateApple(blockEntity);
        }

        public void reset(ScreenBlockEntity blockEntity) {
            badAppleAppView.screenClose(blockEntity);
            badAppleAppView.screenOpen(blockEntity);
        }

        @Override
        public void screenClose(ScreenBlockEntity blockEntity) {
            badAppleAppView.screenClose(blockEntity);
        }
    }

}
