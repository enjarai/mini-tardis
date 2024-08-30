/**
 * This file has been adapted from https://github.com/Patbox/map-canvas-api.
 * See package-info.java for license information.
 */
package dev.enjarai.minitardis.component.screen.canvas.patbox;

import net.minecraft.util.math.ColorHelper;

public class CanvasUtils {
    public static short toLimitedColor(int color) {
        var a = (ColorHelper.Argb.getAlpha(color) >> 4) << 12;
        var r = (ColorHelper.Argb.getRed(color) >> 4) << 8;
        var g = (ColorHelper.Argb.getGreen(color) >> 4) << 4;
        var b = ColorHelper.Argb.getBlue(color) >> 4;

        return (short) (a | r | g | b);
    }

    public static int fromLimitedColor(short color) {
        var r = (color >> 8) & 0xf;
        var g = (color >> 4) & 0xf;
        var b = color & 0xf;

        var r2 = r << 4;
        var g2 = g << 12;
        var b2 = b << 20;

        return r2 | g2 | b2 | 0xff000000;
    }
}
