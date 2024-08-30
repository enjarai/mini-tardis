/**
 * This file has been adapted from https://github.com/Patbox/map-canvas-api.
 * See package-info.java for license information.
 */
package dev.enjarai.minitardis.component.screen.canvas.patbox.font;

public interface LazyCanvasFont extends CanvasFont {
    boolean isLoaded();
    void requestLoad();
}
