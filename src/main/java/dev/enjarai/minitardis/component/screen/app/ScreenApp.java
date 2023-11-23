package dev.enjarai.minitardis.component.screen.app;

import com.mojang.serialization.Codec;
import dev.enjarai.minitardis.block.console.ConsoleScreenBlockEntity;
import dev.enjarai.minitardis.canvas.ModCanvasUtils;
import dev.enjarai.minitardis.component.TardisControl;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ClickType;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.function.Supplier;

public interface ScreenApp {
    Map<Identifier, Codec<? extends ScreenApp>> ALL = Map.of(
            ScannerApp.ID, ScannerApp.CODEC,
            GpsApp.ID, GpsApp.CODEC,
            BadAppleApp.ID, BadAppleApp.CODEC,
            StatusApp.ID, StatusApp.CODEC,
            HistoryApp.ID, HistoryApp.CODEC
    );
    Map<Identifier, Supplier<? extends ScreenApp>> CONSTRUCTORS = Map.of(
            ScannerApp.ID, ScannerApp::new,
            GpsApp.ID, GpsApp::new,
            BadAppleApp.ID, BadAppleApp::new,
            StatusApp.ID, StatusApp::new,
            HistoryApp.ID, HistoryApp::new
    );
    Codec<ScreenApp> CODEC = Identifier.CODEC.dispatch(ScreenApp::id, ALL::get);

    /**
     * Draw the contents of the application to the provided canvas, the canvas provided is limited to the available area.
     * THIS IS CALLED OFF-THREAD, DON'T INTERACT WITH THE WORLD IF AT ALL POSSIBLE.
     */
    void draw(TardisControl controls, ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas);

    default void drawBackground(TardisControl control, ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        CanvasUtils.draw(canvas, 0, 0, ModCanvasUtils.APP_BACKGROUND);
    }

    /**
     * Handle a player clicking on the screen. Coordinates provided are relative to the draw canvas.
     */
    boolean onClick(TardisControl controls, ConsoleScreenBlockEntity blockEntity, ServerPlayerEntity player, ClickType type, int x, int y);

    /**
     * Draw the icon of the application to the provided canvas, the canvas provided is limited to the available area.
     * THIS IS CALLED OFF-THREAD, DON'T INTERACT WITH THE WORLD IF AT ALL POSSIBLE.
     */
    void drawIcon(TardisControl controls, ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas);

    /**
     * Called every tick for every screen displaying this app.
     * Keep in mind multiple screens can display the same app, and will share the app's state.
     */
    default void screenTick(TardisControl controls, ConsoleScreenBlockEntity blockEntity) {
    }

    /**
     * Called when a screen opens this app.
     * Keep in mind multiple screens can display the same app, and will share the app's state.
     */
    default void screenOpen(TardisControl controls, ConsoleScreenBlockEntity blockEntity) {
    }

    /**
     * Called when a screen closes this app.
     * Keep in mind multiple screens can display the same app, and will share the app's state.
     */
    default void screenClose(TardisControl controls, ConsoleScreenBlockEntity blockEntity) {
    }

    Identifier id();
}
