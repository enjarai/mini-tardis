/**
 * This file has been adapted from https://github.com/Patbox/map-canvas-api.
 * See package-info.java for license information.
 */
package dev.enjarai.minitardis.component.screen.canvas.patbox;

public record SubView(DrawableCanvas source, int x1, int y1, int width, int height) implements DrawableCanvas {
    @Override
    public short getRaw(int x, int y) {
        if (x >= 0 && y >= 0 && x < this.width && y < this.height) {
            return this.source.getRaw(x + x1, y + y1);
        }

        return 0;
    }

    @Override
    public void setRaw(int x, int y, short color) {
        if (x >= 0 && y >= 0 && x < this.width && y < this.height) {
            this.source.setRaw(x + x1, y + y1, color);
        }
    }

    @Override
    public void fillRaw(short color) {
        this.source.fillRaw(color);
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public int getWidth() {
        return this.width;
    }
}