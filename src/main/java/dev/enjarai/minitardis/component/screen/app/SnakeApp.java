package dev.enjarai.minitardis.component.screen.app;

import com.mojang.serialization.Codec;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.block.console.ConsoleScreenBlockEntity;
import dev.enjarai.minitardis.canvas.ModCanvasUtils;
import dev.enjarai.minitardis.component.TardisControl;
import dev.enjarai.minitardis.component.screen.element.DimensionStarElement;
import dev.enjarai.minitardis.component.screen.element.SnakeElement;
import eu.pb4.mapcanvas.api.core.CanvasColor;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.api.font.DefaultFonts;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.LocalRandom;
import net.minecraft.util.math.random.Random;

import java.util.Comparator;

public class SnakeApp implements ScreenApp {
    public static final Codec<SnakeApp> CODEC = Codec.unit(SnakeApp::new);
    public static final Identifier ID = MiniTardis.id("snake");

    @Override
    public AppView getView(TardisControl controls) {
        return new ElementHoldingView(controls) {
            private final Random deterministicRandom = new LocalRandom(69420);

            @Override
            public void draw(ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas) {
                super.draw(blockEntity, canvas);
            }

            @Override
            public void screenOpen(ConsoleScreenBlockEntity blockEntity) {
                if (children.isEmpty()) {
                    addElement(new SnakeElement(0, 0, 0, 0));
                }
            }

            @Override
            public void drawBackground(ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas) {
                CanvasUtils.draw(canvas, 0, 0, ModCanvasUtils.DIMENSIONS_BACKGROUND);
            }
        };
    }

    @Override
    public void drawIcon(TardisControl controls, ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        CanvasUtils.draw(canvas, 0, 0, ModCanvasUtils.DIMENSIONS_APP);
    }

    @Override
    public Identifier id() {
        return ID;
    }
}
