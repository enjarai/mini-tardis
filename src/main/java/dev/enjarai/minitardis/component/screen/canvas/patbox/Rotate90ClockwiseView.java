/**
 * This file has been adapted from https://github.com/Patbox/map-canvas-api.
 * See package-info.java for license information.
 */
package dev.enjarai.minitardis.component.screen.canvas.patbox;

public record Rotate90ClockwiseView(DrawableCanvas source) implements DrawableCanvas {
    @Override
    public short getRaw(int x, int y) {
        return this.source.getRaw(y, this.getWidth() - x - 1);
    }

    @Override
    public void setRaw(int x, int y, short color) {
        this.source.setRaw(y, this.getWidth() - x - 1, color);
    }

    @Override
    public void fillRaw(short color) {
        this.source.fillRaw(color);
    }

    @Override
    public int getHeight() {
        return this.source.getWidth();
    }

    @Override
    public int getWidth() {
        return this.source.getHeight();
    }
}