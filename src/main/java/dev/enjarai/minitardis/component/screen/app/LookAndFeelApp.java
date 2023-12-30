package dev.enjarai.minitardis.component.screen.app;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.block.console.ConsoleScreenBlockEntity;
import dev.enjarai.minitardis.canvas.ModCanvasUtils;
import dev.enjarai.minitardis.component.TardisControl;
import eu.pb4.mapcanvas.api.core.CanvasColor;
import eu.pb4.mapcanvas.api.core.CanvasImage;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.api.font.DefaultFonts;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import eu.pb4.mapcanvas.impl.view.SubView;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ClickType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Arrays;
import java.util.stream.IntStream;

public class LookAndFeelApp implements ScreenApp {
    public static final Codec<LookAndFeelApp> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT_STREAM.optionalFieldOf("history", IntStream.empty()).forGetter(app -> IntStream.of(app.history))
    ).apply(instance, LookAndFeelApp::new));
    public static final Identifier ID = MiniTardis.id("look_and_feel");

    public static final int SV_SIZE = 76;
    public static final int H_WIDTH = 16;

    private final int[] history;

    private LookAndFeelApp(int[] history) {
        this.history = history;
    }

    private LookAndFeelApp(IntStream history) {
        this(Arrays.copyOf(history.toArray(), 6));
    }

    public LookAndFeelApp() {
        this(new int[] {CanvasColor.TERRACOTTA_BLUE_LOWEST.getRgbColor(), 0, 0, 0, 0, 0});
    }

    @Override
    public AppView getView(TardisControl controls) {
        return new AppView() {
            boolean initialized;
            float h;
            float s;
            float v;
            @Nullable
            DrawableCanvas pickerCanvas;

            @Override
            public void draw(ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas) {
                if (!initialized) {
                    var hsv = rgbToHsv(blockEntity.backgroundColor.getRgbColor());
                    this.h = hsv[0];
                    this.s = hsv[1];
                    this.v = hsv[2];
                    initialized = true;
                }

                if (pickerCanvas == null) {
                    var pickerPixels = new int[SV_SIZE + 2 + H_WIDTH][SV_SIZE];

                    for (int s = 0; s < SV_SIZE; s++) {
                        for (int v = 0; v < SV_SIZE; v++) {
                            var rgb = MathHelper.hsvToRgb(this.h, s / (float) SV_SIZE, v / (float) SV_SIZE);
                            pickerPixels[s][SV_SIZE - v - 1] = rgb;
                        }
                    }

                    for (int h = 0; h < SV_SIZE; h++) {
                        var rgb = MathHelper.hsvToRgb(h / (float) SV_SIZE, 1, 1);
                        for (int i = 0; i < H_WIDTH; i++) {
                            pickerPixels[SV_SIZE + 2 + i][h] = rgb;
                        }
                    }

                    var pickerCanvas = new CanvasImage(SV_SIZE + 2 + H_WIDTH, SV_SIZE);
                    for (int x = 0; x < SV_SIZE + 2 + H_WIDTH; x++) {
                        for (int y = 0; y < SV_SIZE; y++) {
                            if (x >= SV_SIZE && x < SV_SIZE + 2) continue;
                            pickerCanvas.set(x, y, floydDither(pickerPixels, x, y, pickerPixels[x][y]));
                        }
                    }

                    CanvasUtils.draw(pickerCanvas, 2 + SV_SIZE, (int) (-4 + h * SV_SIZE), ModCanvasUtils.HUE_SELECTOR);
                    this.pickerCanvas = pickerCanvas;
                }

                DefaultFonts.VANILLA.drawText(canvas, "Background Color", 4, 6, 8, CanvasColor.WHITE_HIGH);

                CanvasUtils.draw(canvas, 2, 18, pickerCanvas);

                CanvasUtils.draw(new SubView(canvas, 2, 18, SV_SIZE, SV_SIZE),
                        -8 + (int) (this.s * SV_SIZE), -7 + SV_SIZE - (int) (this.v * SV_SIZE), ModCanvasUtils.SV_SELECTOR);

                var historySize = 11;
                var historyOffset = 2;
                for (int i = 0; i < history.length; i++) {
                    var x = 115;
                    var y = 18 + (historySize + historyOffset) * i;
                    CanvasUtils.fill(canvas, x, y, x + historySize, y + historySize, CanvasUtils.findClosestColor(history[i]));
                }
            }

            @Override
            public void drawBackground(ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas) {
                CanvasUtils.draw(canvas, 0, 0, ModCanvasUtils.LOOK_AND_FEEL_BACKGROUND);
            }

            @Override
            public boolean onClick(ConsoleScreenBlockEntity blockEntity, ServerPlayerEntity player, ClickType type, int x, int y) {
                if (y >= 18 && y < 18 + SV_SIZE) {
                    if (x >= 2 && x < 2 + SV_SIZE) {
                        this.s = (x - 2) / (float) SV_SIZE;
                        this.v = (SV_SIZE - (y - 19)) / (float) SV_SIZE;

                        blockEntity.backgroundColor = CanvasUtils.findClosestColor(MathHelper.hsvToRgb(this.h, this.s, this.v));
                        blockEntity.playClickSound(1);
                        return true;
                    } else if (x >= 4 + SV_SIZE && x < 4 + SV_SIZE + H_WIDTH) {
                        this.h = (y - 18) / (float) SV_SIZE;
                        this.pickerCanvas = null;

                        blockEntity.backgroundColor = CanvasUtils.findClosestColor(MathHelper.hsvToRgb(this.h, this.s, this.v));
                        blockEntity.playClickSound(0.9f);
                        return true;
                    } else if (x >= 115 && x < 115 + 11) {
                        for (int i = 0; i < history.length; i++) {
                            var cy = 18 + (11 + 2) * i;
                            if (y >= cy && y < cy + 11) {
                                var color = history[i];
                                var hsv = rgbToHsv(color);

                                this.h = hsv[0];
                                this.s = hsv[1];
                                this.v = hsv[2];
                                this.pickerCanvas = null;

                                blockEntity.backgroundColor = CanvasUtils.findClosestColor(color);
                                blockEntity.playClickSound(1.1f);
                                return true;
                            }
                        }
                    }
                }

                return false;
            }

            @Override
            public void screenClose(ConsoleScreenBlockEntity blockEntity) {
                var newColor = CanvasUtils.findClosestColor(MathHelper.hsvToRgb(this.h, this.s, this.v)).getRgbColor();
                if (newColor != history[0]) {
                    // Shift the history over
                    for (int i = history.length - 1; i > 0; i--) {
                        history[i] = history[i - 1];
                    }

                    history[0] = newColor;
                }
            }
        };
    }

    // Sourced from the splendid Image2Map mod by Patbox.
    // https://github.com/Patbox/Image2Map/blob/1.20.2/src/main/java/space/essem/image2map/renderer/MapRenderer.java
    private static CanvasColor floydDither(int[][] pixels, int x, int y, int imageColor) {
        var closestColor = CanvasUtils.findClosestColor(imageColor);
        var palletedColor = closestColor.getRgbColor();

        var errorR = ColorHelper.Argb.getRed(imageColor) - ColorHelper.Argb.getRed(palletedColor);
        var errorG = ColorHelper.Argb.getGreen(imageColor) - ColorHelper.Argb.getGreen(palletedColor);
        var errorB = ColorHelper.Argb.getBlue(imageColor) - ColorHelper.Argb.getBlue(palletedColor);
        if (pixels[0].length > y + 1) {
            pixels[x][y + 1] = applyError(pixels[x][y + 1], errorR, errorG, errorB, 7.0 / 16.0);
        }
        if (pixels.length > x + 1) {
            if (y > 0) {
                pixels[x + 1][y - 1] = applyError(pixels[x + 1][y - 1], errorR, errorG, errorB, 3.0 / 16.0);
            }
            pixels[x + 1][y] = applyError(pixels[x + 1][y], errorR, errorG, errorB, 5.0 / 16.0);
            if (pixels[0].length > y + 1) {
                pixels[x + 1][y + 1] = applyError(pixels[x + 1][y + 1], errorR, errorG, errorB, 1.0 / 16.0);
            }
        }

        return closestColor;
    }

    private static int applyError(int pixelColor, int errorR, int errorG, int errorB, double quantConst) {
        int pR = MathHelper.clamp(ColorHelper.Argb.getRed(pixelColor) + (int) ((double) errorR * quantConst), 0, 255);
        int pG = MathHelper.clamp(ColorHelper.Argb.getGreen(pixelColor) + (int) ((double) errorG * quantConst), 0, 255);
        int pB = MathHelper.clamp(ColorHelper.Argb.getBlue(pixelColor) + (int) ((double) errorB * quantConst), 0, 255);
        return ColorHelper.Argb.getArgb(ColorHelper.Argb.getAlpha(pixelColor), pR, pG, pB);
    }

    private static float[] rgbToHsv(int rgb) {
        var r = ColorHelper.Argb.getRed(rgb);
        var g = ColorHelper.Argb.getGreen(rgb);
        var b = ColorHelper.Argb.getBlue(rgb);
        return Color.RGBtoHSB(r, g, b, null);
    }

    @Override
    public void drawIcon(TardisControl controls, ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        CanvasUtils.draw(canvas, 0, 0, ModCanvasUtils.LOOK_AND_FEEL_APP);
    }

    @Override
    public Identifier id() {
        return ID;
    }
}
