package dev.enjarai.minitardis.component.screen.app;

import dev.enjarai.minitardis.block.console.ConsoleScreenBlockEntity;
import dev.enjarai.minitardis.canvas.ModCanvasUtils;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ClickType;

/**
 * Holds transient, screen specific state for an app.
 */
public interface AppView {
    /**
     * Draw the contents of the application to the provided canvas, the canvas provided is limited to the available area.
     * THIS IS CALLED OFF-THREAD, DON'T INTERACT WITH THE WORLD IF AT ALL POSSIBLE.
     */
    void draw(ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas);

    default void drawBackground(ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        CanvasUtils.draw(canvas, 0, 0, ModCanvasUtils.APP_BACKGROUND);
    }

    /**
     * Handle a player clicking on the screen. Coordinates provided are relative to the draw canvas.
     */
    boolean onClick(ConsoleScreenBlockEntity blockEntity, ServerPlayerEntity player, ClickType type, int x, int y);

    /**
     * Called every tick for every screen displaying this app.
     * Keep in mind multiple screens can display the same app, and will share the app's state.
     */
    default void screenTick(ConsoleScreenBlockEntity blockEntity) {
    }

    /**
     * Called when a screen opens this app.
     * Keep in mind multiple screens can display the same app, and will share the app's state.
     */
    default void screenOpen(ConsoleScreenBlockEntity blockEntity) {
    }

    /**
     * Called when a screen closes this app.
     * Keep in mind multiple screens can display the same app, and will share the app's state.
     */
    default void screenClose(ConsoleScreenBlockEntity blockEntity) {
    }
}
