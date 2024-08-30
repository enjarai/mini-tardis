/**
 * This file has been adapted from https://github.com/Patbox/map-canvas-api.
 * See package-info.java for license information.
 */
package dev.enjarai.minitardis.component.screen.canvas.patbox.font.impl;

import dev.enjarai.minitardis.component.screen.canvas.patbox.DrawableCanvas;
import dev.enjarai.minitardis.component.screen.canvas.patbox.font.CanvasFont;

import java.awt.*;
import java.awt.font.FontRenderContext;

public record AwtFont(Font font, Metadata metadata) implements CanvasFont {
    private static final FontRenderContext CONTEXT = new FontRenderContext(null, false, false);

    @Override
    public int getTextWidth(String text, double size) {
        return (int) font.deriveFont((float) size).getStringBounds(text, CONTEXT).getWidth();

    }

    @Override
    public int getGlyphWidth(int character, double size, int offset) {
        return (int) (font.deriveFont((float) size).getStringBounds(Character.toString(character), new FontRenderContext(null, false, false)).getWidth() + offset * size);
    }

    @Override
    public int drawGlyph(DrawableCanvas canvas, int character, int x, int y, double size, int offset, short color) {
        var font = this.font.deriveFont((float) size);
        var shape = font.createGlyphVector(CONTEXT, new int[] { character }).getOutline();

        var bounds = shape.getBounds();

        for (int xs = 0; xs < bounds.width; xs++) {
            for (int ys = 0; ys < bounds.height; ys++) {
                if (shape.contains(xs + bounds.x, ys + bounds.y)) {
                    canvas.setRaw(xs + x , ys + y , color);
                }
            }
        }

        return (int) (bounds.width + offset * size);
    }

    @Override
    public void drawText(DrawableCanvas canvas, String text, int x, int y, double size, short color) {
        var font = this.font.deriveFont((float) size);
        var shape = font.createGlyphVector(CONTEXT, text).getOutline();

        var bounds = shape.getBounds();

        for (int xs = 0; xs < bounds.width; xs++) {
            for (int ys = 0; ys < bounds.height; ys++) {
                if (shape.contains(xs + bounds.x, ys + bounds.y)) {
                    canvas.setRaw(xs + x , ys + y , color);
                }
            }
        }
    }

    @Override
    public boolean containsGlyph(int character) {
        return this.font.canDisplay(character);
    }

    @Override
    public Metadata getMetadata() {
        return this.metadata;
    }
}
