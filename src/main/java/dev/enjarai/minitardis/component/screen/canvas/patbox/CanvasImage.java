/**
 * This file has been adapted from https://github.com/Patbox/map-canvas-api.
 * See package-info.java for license information.
 */
package dev.enjarai.minitardis.component.screen.canvas.patbox;

import dev.enjarai.minitardis.ImGoingToStabBasique;
import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;

import java.awt.image.BufferedImage;
import java.util.Arrays;

public final class CanvasImage implements DrawableCanvas {
    public static final StructEndec<CanvasImage> ENDEC = StructEndecBuilder.of(
            Endec.INT.fieldOf("width", i -> i.width),
            Endec.INT.fieldOf("height", i -> i.height),
            ImGoingToStabBasique.DEEZ.fieldOf("data", i -> i.data),
            CanvasImage::new
    );

    private final int width;
    private final int height;
    private final short[] data;

    public CanvasImage(int width, int height) {
        this(width, height, new short[width * height]);
    }

    private CanvasImage(int width, int height, short[] data) {
        this.data = data;
        this.width = width;
        this.height = height;
    }

    public static CanvasImage from(BufferedImage image) {
        var width = image.getWidth();
        var height = image.getHeight();

        var canvas = new CanvasImage(image.getWidth(), image.getHeight());

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                var color = image.getRGB(x, y);
                canvas.setRaw(x, y, CanvasUtils.toLimitedColor(color));
            }
        }

        return canvas;
    }

    @Override
    public short getRaw(int x, int y) {
        if (x >= this.width || y >= this.height || x < 0 || y < 0) {
            return 0;
        }

        return this.data[x + y * this.width];
    }

    @Override
    public void setRaw(int x, int y, short color) {
        if (x >= this.width || y >= this.height || x < 0 || y < 0) {
            return;
        }

        this.data[x + y * this.width] = color;
    }

    @Override
    public void fillRaw(short color) {
        Arrays.fill(this.data, color);
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CanvasImage that)) return false;

        return width == that.width && height == that.height && Arrays.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        int result = width;
        result = 31 * result + height;
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }
}