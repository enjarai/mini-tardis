/**
 * This file has been adapted from https://github.com/Patbox/map-canvas-api.
 * See package-info.java for license information.
 */
package dev.enjarai.minitardis.component.screen.canvas.patbox.font.impl;

import dev.enjarai.minitardis.component.screen.canvas.patbox.DrawableCanvas;
import dev.enjarai.minitardis.component.screen.canvas.patbox.font.CanvasFont;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record StackedFont(CanvasFont[] fonts, Metadata metadata) implements CanvasFont {

    public StackedFont(CanvasFont[] fonts) {
        this(fonts, Metadata.create(fonts[0].getMetadata().name(), ((Supplier<List<String>>) () -> {
            var list = new ArrayList<String>();
            for (var font : fonts) {
                if (font == fonts[0]) {
                    list.addAll(font.getMetadata().authors());
                } else {
                    for (var author : font.getMetadata().authors()) {
                        list.add(author + " (" + font.getMetadata().name() + ")");
                    }
                }
            }
            return list;
        }).get(), fonts[0].getMetadata().description().orElse(null)));
    }

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
}
