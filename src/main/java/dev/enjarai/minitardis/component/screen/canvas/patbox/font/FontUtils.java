/**
 * This file has been adapted from https://github.com/Patbox/map-canvas-api.
 * See package-info.java for license information.
 */
package dev.enjarai.minitardis.component.screen.canvas.patbox.font;

import dev.enjarai.minitardis.component.screen.canvas.patbox.font.impl.AwtFont;
import dev.enjarai.minitardis.component.screen.canvas.patbox.font.impl.BitmapFont;
import dev.enjarai.minitardis.component.screen.canvas.patbox.font.impl.StackedFont;
import dev.enjarai.minitardis.component.screen.canvas.patbox.font.impl.serialization.RawBitmapFontSerializer;
import dev.enjarai.minitardis.component.screen.canvas.patbox.font.impl.serialization.UniHexFontReader;
import dev.enjarai.minitardis.component.screen.canvas.patbox.font.impl.serialization.VanillaFontReader;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;


import java.awt.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.function.Function;
import java.util.zip.ZipFile;

public final class FontUtils {
    private FontUtils() {}
    /**
     * Merges multiple fonts into one, allowing to stack/fill up possible missing characters
     *
     * @param fonts fonts to merge
     * @return single font with merged characters
     */
    public static CanvasFont merge(CanvasFont... fonts) {
        return new StackedFont(fonts);
    }

    /**
     * Merges multiple fonts into one, allowing to stack/fill up possible missing characters
     *
     * @param fonts fonts to merge
     * @return single font with merged characters
     */
    public static CanvasFont merge(CanvasFont.Metadata metadata, CanvasFont... fonts) {
        return new StackedFont(fonts, metadata);
    }

    /**
     * Creates new font from vanilla definitions
     * You can stack them to fill missing entries or use vanilla json definitions
     *
     * @param identifier font's identifier
     * @param zipFile sources
     * @return Font
     */
    public static CanvasFont fromVanillaFormat(Identifier identifier, ZipFile... zipFile) {
        return VanillaFontReader.build(zipFile, identifier);
    }

    /**
     * Creates new font from vanilla definitions
     * You can stack them to fill missing entries or use vanilla json definitions
     *
     * @param identifier font's identifier
     * @param metadata font's metadata
     * @param zipFile sources
     * @return Font
     */
    public static CanvasFont fromVanillaFormat(Identifier identifier, CanvasFont.Metadata metadata, ZipFile... zipFile) {
        return VanillaFontReader.build(zipFile, metadata, identifier);
    }

    /**
     * Creates new font from vanilla definitions
     * You can stack them to fill missing entries or use vanilla json definitions
     *
     * @param identifier font's identifier
     * @param metadata font's metadata
     * @param fileGetter file reading code
     * @return Font
     */
    public static CanvasFont fromVanillaFormat(Identifier identifier, CanvasFont.Metadata metadata, Function<String, @Nullable InputStream> fileGetter) {
        return VanillaFontReader.build(fileGetter, metadata, identifier);
    }

    /**
     * Creates new font from Unifont's .hex format
     * You can stack them to fill missing entries or use vanilla json definitions
     *
     * @param fontFile stream reading .hex file
     * @param metadata font's metadata
     * @return Font
     */
    public static CanvasFont fromUniHexFormat(InputStream fontFile, CanvasFont.Metadata metadata) {
        try {
            return UniHexFontReader.build(fontFile, metadata);
        } catch (Throwable e) {
            e.printStackTrace();
            return BitmapFont.EMPTY;
        }
    }

    /**
     * Reads font from Map Canvas API Font format
     * @param stream stream for files/bytes of font
     * @return New canvas font
     */
    public static CanvasFont fromMapCanvasFontFormat(InputStream stream) {
        var font = RawBitmapFontSerializer.read(stream);
        return font != null ? font : BitmapFont.EMPTY;
    }

    /**
     * Creates canvas font from
     * @param font Awt Font used as a base
     * @return New canvas font
     */
    public static CanvasFont fromAwtFont(Font font) {
        return new AwtFont(font, CanvasFont.Metadata.create(font.getName(), List.of(), "A font"));
    }

    /**
     * Creates canvas font from
     * @param font Awt Font used as a base
     * @param metadata Metadata used by font
     * @return New canvas font
     */
    public static CanvasFont fromAwtFont(Font font, CanvasFont.Metadata metadata) {
        return new AwtFont(font, metadata);
    }

    /**
     * Writes font to Map Canvas API Font format
     * Only works with bitmap fonts
     *
     * @param font font to convert
     * @param stream stream it will be written to
     * @return true for successful conversion, otherwise false
     */
    public static boolean toMapCanvasFontFormat(CanvasFont font, OutputStream stream) {
        if (font instanceof BitmapFont bitmapFont) {
            return RawBitmapFontSerializer.write(bitmapFont, stream);
        }
        return false;
    }
}