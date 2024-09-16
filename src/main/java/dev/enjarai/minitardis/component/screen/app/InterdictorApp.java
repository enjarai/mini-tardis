package dev.enjarai.minitardis.component.screen.app;

import com.mojang.serialization.Codec;
import dev.enjarai.minitardis.block.console.ScreenBlockEntity;
import dev.enjarai.minitardis.canvas.TardisCanvasUtils;
import dev.enjarai.minitardis.component.FlightWave;
import dev.enjarai.minitardis.component.TardisControl;
import dev.enjarai.minitardis.component.screen.canvas.patbox.DrawableCanvas;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ClickType;

public class InterdictorApp implements ScreenApp {
    public static final Codec<InterdictorApp> CODEC = Codec.unit(InterdictorApp::new);

    @Override
    public AppView getView(TardisControl controls) {
        return new AppView() {
            FlightWave selectedWave = new FlightWave(controls.getTardis().getRandom());

            @Override
            public void draw(ScreenBlockEntity blockEntity, DrawableCanvas canvas) {
                var timeyTimey = controls.getTardis().getInteriorWorld().getTime() * 2;

                drawWave(canvas, selectedWave, timeyTimey, (short) 0xf383);
            }

            void drawWave(DrawableCanvas canvas, FlightWave wave, double offset, short color) {
                for (int x = 3; x < 126; x++) {
                    int y1 = 55 + (int) (Math.sin((x + offset + wave.getOffset() * 35.0 - 1) * wave.getPeriod() * 0.2) * 35.0 * wave.getMagnitude());
                    int y2 = 55 + (int) (Math.sin((x + offset + wave.getOffset() * 35.0) * wave.getPeriod() * 0.2) * 35.0 * wave.getMagnitude());
                    canvas.bresenhamLine(x - 1, y1, x, y2, color);
                }
            }

            @Override
            public void drawBackground(ScreenBlockEntity blockEntity, DrawableCanvas canvas) {
                canvas.draw(0, 0, TardisCanvasUtils.getSprite("interdictor_background"));
            }

            @Override
            public boolean onClick(ScreenBlockEntity blockEntity, ServerPlayerEntity player, ClickType type, int x, int y) {
                return false;
            }
        };
    }

    @Override
    public ScreenAppType<?> getType() {
        return ScreenAppTypes.INTERDICTOR;
    }
}
