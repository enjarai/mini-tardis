package dev.enjarai.minitardis.component.screen.app;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.enjarai.minitardis.block.console.ScreenBlockEntity;
import dev.enjarai.minitardis.canvas.TardisCanvasUtils;
import dev.enjarai.minitardis.component.TardisControl;
import dev.enjarai.minitardis.component.screen.canvas.CanvasColors;
import dev.enjarai.minitardis.component.screen.canvas.patbox.DrawableCanvas;
import dev.enjarai.minitardis.component.screen.canvas.patbox.font.DefaultFonts;
import dev.enjarai.minitardis.component.screen.element.DimensionStarElement;
import dev.enjarai.minitardis.component.screen.element.SmallButtonElement;
import dev.enjarai.minitardis.data.RandomAppLootFunction;
import net.minecraft.loot.context.LootContext;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.random.LocalRandom;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DimensionsApp implements ScreenApp {
    public static final Codec<DimensionsApp> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RegistryKey.createCodec(RegistryKeys.WORLD).listOf().optionalFieldOf("accessible_dimensions", List.of()).forGetter(app -> app.accessibleDimensions)
    ).apply(instance, DimensionsApp::new));

    public final List<RegistryKey<World>> accessibleDimensions;

    private DimensionsApp(List<RegistryKey<World>> accessibleDimensions) {
        this.accessibleDimensions = new ArrayList<>(accessibleDimensions);
    }

    public DimensionsApp() {
        this.accessibleDimensions = new ArrayList<>();
    }

    @Override
    public AppView getView(TardisControl controls) {
        return new ElementHoldingView(controls) {
            private final Random deterministicRandom = new LocalRandom(69420);
            private final SmallButtonElement saveDimButton = addElement(new SmallButtonElement(68, 2, "Add", controls1 -> {
                controls1.getTardis().getDestination().ifPresent(destination -> {
                    if (!accessibleDimensions.contains(destination.worldKey())) {
                        accessibleDimensions.add(destination.worldKey());
                        refreshStars();
                    }
                });
            }));

            {
                controls.getTardis().getServer().getWorldRegistryKeys().stream()
                        .filter(key -> canAddAsAccessible(key))
                        .sorted(Comparator.comparing(RegistryKey::getValue))
                        .forEachOrdered(world -> {
                            var star = new DimensionStarElement(0, 0, world);
                            do {
                                star.x = deterministicRandom.nextBetween(2, 128 - 2 - 11);
                                star.y = deterministicRandom.nextBetween(18, 96 - 2 - 11);
                            } while (children.stream().anyMatch(el -> el instanceof DimensionStarElement pel && star.overlapsWith(pel)));

                            star.visible = accessibleDimensions.contains(world);
                            addElement(star);
                        });
            }

            @Override
            public void draw(ScreenBlockEntity blockEntity, DrawableCanvas canvas) {
                saveDimButton.visible = false;

                controls.getTardis().getDestination().ifPresentOrElse(destination -> {
                    var worldId = destination.worldKey();

                    if (!accessibleDimensions.contains(worldId)) {
                        saveDimButton.visible = true;
                        DefaultFonts.VANILLA.drawText(canvas, "Unknown", 5, 6, 8, CanvasColors.LIGHT_GRAY);
                    } else {
                        DefaultFonts.VANILLA.drawText(
                                canvas, translateWorldId(worldId).getString(),
                                5, 6, 8, CanvasColors.LIGHT_GRAY
                        );
                    }
                }, () -> {
                    DefaultFonts.VANILLA.drawText(canvas, "None", 5, 6, 8, CanvasColors.LIGHT_GRAY);
                });


                super.draw(blockEntity, canvas);
            }

            private void refreshStars() {
                for (var element : children()) {
                    if (element instanceof DimensionStarElement star) {
                        star.visible = accessibleDimensions.contains(star.worldKey);
                    }
                }
            }

            @Override
            public void drawBackground(ScreenBlockEntity blockEntity, DrawableCanvas canvas) {
                canvas.draw(0, 0, TardisCanvasUtils.getSprite("dimensions_background"));
            }
        };
    }

    public boolean canAddAsAccessible(RegistryKey<World> worldKey) {
        return !worldKey.getValue().getPath().startsWith("tardis/");
    }

    @SuppressWarnings("deprecation")
    public static Text translateWorldId(RegistryKey<World> key) {
        var id = key.getValue();
        return Text.translatableWithFallback("dimension." + id.getNamespace() + "." + id.getPath(),
                WordUtils.capitalize(id.getPath().replace('_', ' ')));
    }

    @Override
    public void drawIcon(TardisControl controls, ScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        canvas.draw(0, 0, TardisCanvasUtils.getSprite("app/dimensions"));
    }

    @Override
    public void appendTooltip(List<Text> tooltip) {
        for (var dimension : accessibleDimensions) {
            tooltip.add(Text.literal(" ").append(translateWorldId(dimension))
                    .fillStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY)));
        }
    }

    @Override
    public void applyLootModifications(LootContext context, RandomAppLootFunction lootFunction) {
        accessibleDimensions.add(context.getWorld().getRegistryKey());
        for (var dimension : lootFunction.additionalDimensions()) {
            if (!accessibleDimensions.contains(dimension)) {
                accessibleDimensions.add(dimension);
            }
        }
    }

    @Override
    public ScreenAppType<?> getType() {
        return ScreenAppTypes.DIMENSIONS;
    }
}
