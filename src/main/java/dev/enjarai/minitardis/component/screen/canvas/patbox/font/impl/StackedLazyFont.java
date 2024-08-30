/**
 * This file has been adapted from https://github.com/Patbox/map-canvas-api.
 * See package-info.java for license information.
 */
package dev.enjarai.minitardis.component.screen.canvas.patbox.font.impl;

import dev.enjarai.minitardis.component.screen.canvas.patbox.DrawableCanvas;
import dev.enjarai.minitardis.component.screen.canvas.patbox.font.LazyCanvasFont;

public record StackedLazyFont(LazyCanvasFont[] fonts, Metadata metadata) implements LazyCanvasFont {
    @Override
    public int getGlyphWidth(int character, double size, int offset) {
        for (var font : fonts) {
            if (font.containsGlyph(character)) {
                return font.getGlyphWidth(character, size, offset);
            }
        }

        return BitmapFont.EMPTY.getGlyphWidth(character, size, offset);
    }

    @Override
    public int drawGlyph(DrawableCanvas canvas, int character, int x, int y, double size, int offset, short color) {
        for (var font : fonts) {
            if (font.containsGlyph(character)) {
                return font.drawGlyph(canvas, character, x, y, size, offset, color);
            }
        }

        return BitmapFont.EMPTY.getGlyphWidth(character, size, offset);
    }

    @Override
    public boolean containsGlyph(int character) {
        for (var font : fonts) {
            if (font.containsGlyph(character)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Metadata getMetadata() {
        return this.metadata;
    }

    @Override
    public boolean isLoaded() {
        for (var x : this.fonts) {
            if (!x.isLoaded()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void requestLoad() {
        for (var x : this.fonts) {
            x.requestLoad();
        }
    }
}
