/**
 * This file has been adapted from https://github.com/Patbox/map-canvas-api.
 * See package-info.java for license information.
 */
package dev.enjarai.minitardis.component.screen.canvas.patbox;

import java.awt.image.BufferedImage;

public interface DrawableCanvas {
    short getRaw(int x, int y);

    void setRaw(int x, int y, short color);

    default void fillRaw(short color) {
        int width = this.getWidth();
        int height = this.getHeight();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                this.setRaw(x, y, color);
            }
        }
    }

    int getHeight();

    int getWidth();

    default CanvasImage copy() {
        final var width = this.getWidth();
        final var height = this.getHeight();
        final var image = new CanvasImage(width, height);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRaw(x, y, this.getRaw(x, y));
            }
        }

        return image;
    }

    default CanvasImage copy(int x, int y, int width, int height) {
        final var newWidth = Math.min(this.getWidth() - x, width);
        final var newHeight = Math.min(this.getHeight() - y, height);
        final var image = new CanvasImage(newWidth, newHeight);

        for (int lx = 0; lx < newWidth; lx++) {
            for (int ly = 0; ly < newHeight; ly++) {
                image.setRaw(lx, ly, this.getRaw(x + lx, y + ly));
            }
        }

        return image;
    }

    default BufferedImage toImage() {
        var image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        final int width = getWidth();
        final int height = getHeight();

        for (var x = 0; x < width; x++) {
            for (var y = 0; y < height; y++) {
                var color = getRaw(x, y);

                image.setRGB(x, y, CanvasUtils.ARGBFromLimitedColor(color) | 0xFF000000);
            }
        }
        return image;
    }

    default void fillRaw(int x1, int y1, int x2, int y2, short color) {
        final int minX = Math.min(x1, x2);
        final int minY = Math.min(y1, y2);
        final int maxX = Math.max(x1, x2);
        final int maxY = Math.max(y1, y2);

        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                setRaw(x, y, color);
            }
        }
    }

    default void draw(int x, int y, DrawableCanvas source) {
        final int width = source.getWidth();
        final int height = source.getHeight();

        for (int lx = 0; lx < width; lx++) {
            for (int ly = 0; ly < height; ly++) {
                short color = source.getRaw(lx, ly);

                if (color != 0) {
                    setRaw(lx + x, ly + y, color);
                }
            }
        }
    }

    default void draw(int x, int y, int width, int height, DrawableCanvas source) {
        final int baseWidth = source.getWidth();
        final int baseHeight = source.getHeight();

        final double deltaX = (double) baseWidth / width;
        final double deltaY = (double) baseHeight / height;

        for (int lx = 0; lx < width; lx++) {
            for (int ly = 0; ly < height; ly++) {
                short color = source.getRaw((int) (lx * deltaX), (int) (ly * deltaY));

                if (color != 0) {
                    setRaw(lx + x, ly + y, color);
                }
            }
        }
    }

    default void bresenhamLine(int x0, int y0, int x1, int y1, short color) {

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);

        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;

        int err = dx - dy;
        int e2;

        while (true) {

            setRaw(x0, y0, color);

            if (x0 == x1 && y0 == y1) break;

            e2 = 2 * err;

            if (e2 > -dy) {
                err = err - dy;
                x0 = x0 + sx;
            }

            if (e2 < dx) {
                err = err + dx;
                y0 = y0 + sy;
            }
        }
    }
}
