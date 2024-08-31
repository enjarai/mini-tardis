package dev.enjarai.minitardis.component.screen.app;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.enjarai.minitardis.block.console.ScreenBlockEntity;
import dev.enjarai.minitardis.canvas.TardisCanvasUtils;
import dev.enjarai.minitardis.component.TardisControl;
import dev.enjarai.minitardis.component.screen.canvas.CanvasColors;
import dev.enjarai.minitardis.component.screen.canvas.patbox.CanvasImage;
import dev.enjarai.minitardis.component.screen.canvas.patbox.CanvasUtils;
import dev.enjarai.minitardis.component.screen.canvas.patbox.DrawableCanvas;
import dev.enjarai.minitardis.component.screen.canvas.patbox.SubView;
import dev.enjarai.minitardis.component.screen.canvas.patbox.font.DefaultFonts;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ClickType;
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
        this(new int[] {CanvasUtils.ARGBFromLimitedColor(CanvasColors.BACKGROUND), 0, 0, 0, 0, 0});
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
            public void draw(ScreenBlockEntity blockEntity, DrawableCanvas canvas) {
                if (!initialized) {
                    var hsv = rgbToHsv(CanvasUtils.ARGBFromLimitedColor(blockEntity.backgroundColor));
                    this.h = hsv[0];
                    this.s = hsv[1];
                    this.v = hsv[2];
                    initialized = true;
                }

                if (pickerCanvas == null) {
                    var pickerPixels = new int[SV_SIZE + 2 + H_WIDTH][SV_SIZE];

                    for (int s = 0; s < SV_SIZE; s++) {
                        for (int v = 0; v < SV_SIZE; v++) {
                            var rgb = MathHelper.hsvToArgb(this.h, s / (float) SV_SIZE, v / (float) SV_SIZE, 255);
                            pickerPixels[s][SV_SIZE - v - 1] = rgb;
                        }
                    }

                    for (int h = 0; h < SV_SIZE; h++) {
                        var rgb = MathHelper.hsvToArgb(h / (float) SV_SIZE, 1, 1, 255);
                        for (int i = 0; i < H_WIDTH; i++) {
                            pickerPixels[SV_SIZE + 2 + i][h] = rgb;
                        }
                    }

                    var pickerCanvas = new CanvasImage(SV_SIZE + 2 + H_WIDTH, SV_SIZE);
                    for (int x = 0; x < SV_SIZE + 2 + H_WIDTH; x++) {
                        for (int y = 0; y < SV_SIZE; y++) {
                            if (x >= SV_SIZE && x < SV_SIZE + 2) continue;
                            pickerCanvas.setRaw(x, y, CanvasUtils.toLimitedColor(pickerPixels[x][y]));// floydDither(pickerPixels, x, y, pickerPixels[x][y]));
                        }
                    }

                    pickerCanvas.draw(2 + SV_SIZE, (int) (-4 + h * SV_SIZE), TardisCanvasUtils.getSprite("hue_selector"));
                    this.pickerCanvas = pickerCanvas;
                }

                DefaultFonts.VANILLA.drawText(canvas, "Background Color", 4, 6, 8, CanvasColors.WHITE);

                canvas.draw(2, 18, pickerCanvas);

                new SubView(canvas, 2, 18, SV_SIZE, SV_SIZE).draw(
                        -8 + (int) (this.s * SV_SIZE), -7 + SV_SIZE - (int) (this.v * SV_SIZE), TardisCanvasUtils.getSprite("sv_selector"));

                var historySize = 11;
                var historyOffset = 2;
                for (int i = 0; i < history.length; i++) {
                    var x = 115;
                    var y = 18 + (historySize + historyOffset) * i;
                    canvas.fillRaw(x, y, x + historySize, y + historySize, CanvasUtils.toLimitedColor(history[i]));
                }
            }

            @Override
            public void drawBackground(ScreenBlockEntity blockEntity, DrawableCanvas canvas) {
                canvas.draw(0, 0, TardisCanvasUtils.getSprite("look_and_feel_background"));
            }

            @Override
            public boolean onClick(ScreenBlockEntity blockEntity, ServerPlayerEntity player, ClickType type, int x, int y) {
                if (y >= 18 && y < 18 + SV_SIZE) {
                    if (x >= 2 && x < 2 + SV_SIZE) {
                        this.s = (x - 2) / (float) SV_SIZE;
                        this.v = (SV_SIZE - (y - 19)) / (float) SV_SIZE;

                        blockEntity.backgroundColor = CanvasUtils.toLimitedColor(MathHelper.hsvToArgb(this.h, this.s, this.v, 255));
                        blockEntity.playClickSound(1);
                        return true;
                    } else if (x >= 4 + SV_SIZE && x < 4 + SV_SIZE + H_WIDTH) {
                        this.h = (y - 18) / (float) SV_SIZE;
                        this.pickerCanvas = null;

                        blockEntity.backgroundColor = CanvasUtils.toLimitedColor(MathHelper.hsvToArgb(this.h, this.s, this.v, 255));
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

                                blockEntity.backgroundColor = CanvasUtils.toLimitedColor(color);
                                blockEntity.playClickSound(1.1f);
                                return true;
                            }
                        }
                    }
                }

                return false;
            }

            @Override
            public void screenClose(ScreenBlockEntity blockEntity) {
                var newColor = CanvasUtils.ARGBFromLimitedColor(CanvasUtils.toLimitedColor(MathHelper.hsvToArgb(this.h, this.s, this.v, 255)));
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
    private static short floydDither(int[][] pixels, int x, int y, int imageColor) {
        var closestColor = CanvasUtils.toLimitedColor(imageColor);
        var palletedColor = CanvasUtils.ARGBFromLimitedColor(closestColor);

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
    public void drawIcon(TardisControl controls, ScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        canvas.draw(0, 0, TardisCanvasUtils.getSprite("app/look_and_feel"));
    }

    @Override
    public ScreenAppType<?> getType() {
        return ScreenAppTypes.LOOK_AND_FEEL;
    }
}
