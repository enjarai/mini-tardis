package dev.enjarai.minitardis.component.screen.app;

import com.mojang.serialization.Codec;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.block.console.ConsoleScreenBlockEntity;
import dev.enjarai.minitardis.canvas.ModCanvasUtils;
import dev.enjarai.minitardis.component.TardisControl;
import dev.enjarai.minitardis.component.screen.element.DimensionStarElement;
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

public class DimensionsApp extends ElementHoldingApp {
    public static final Codec<DimensionsApp> CODEC = Codec.unit(DimensionsApp::new);
    public static final Identifier ID = MiniTardis.id("dimensions");

    private final Random deterministicRandom = new LocalRandom(69420);

    @Override
    public void draw(TardisControl controls, ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        controls.getTardis().getDestination().ifPresentOrElse(destination -> {
            var worldId = destination.worldKey().getValue();
            DefaultFonts.VANILLA.drawText(
                    canvas, Text.translatable("dimension." + worldId.getNamespace() + "." + worldId.getPath()).getString(),
                    5, 6, 8, CanvasColor.LIGHT_GRAY_HIGH
            );
        }, () -> {
            DefaultFonts.VANILLA.drawText(canvas, "None", 5, 6, 8, CanvasColor.LIGHT_GRAY_HIGH);
        });


        super.draw(controls, blockEntity, canvas);
    }

    @Override
    public void screenOpen(TardisControl controls, ConsoleScreenBlockEntity blockEntity) {
        if (children.isEmpty()) {
            controls.getTardis().getServer().getWorldRegistryKeys().stream()
                    .filter(key -> !key.getValue().getPath().startsWith("tardis/"))
                    .sorted(Comparator.comparing(RegistryKey::getValue))
                    .forEachOrdered(world -> {
                        var x = deterministicRandom.nextBetween(2, 128 - 2 - 11);
                        var y = deterministicRandom.nextBetween(18, 96 - 2 - 11);
                        addElement(new DimensionStarElement(x, y, world));
                    });
        }
    }

    @Override
    public void drawBackground(TardisControl control, ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        CanvasUtils.draw(canvas, 0, 0, ModCanvasUtils.DIMENSIONS_BACKGROUND);
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
