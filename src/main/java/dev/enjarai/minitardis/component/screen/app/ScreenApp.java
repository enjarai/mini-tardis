package dev.enjarai.minitardis.component.screen.app;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import dev.enjarai.minitardis.block.console.ConsoleScreenBlockEntity;
import dev.enjarai.minitardis.component.TardisControl;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.function.Supplier;

public interface ScreenApp {
    Map<Identifier, Codec<? extends ScreenApp>> ALL = new ImmutableMap.Builder<Identifier, Codec<? extends ScreenApp>>() {{
            put(SnakeApp.ID, SnakeApp.CODEC);
            put(ScannerApp.ID, ScannerApp.CODEC);
            put(GpsApp.ID, GpsApp.CODEC);
            put(BadAppleApp.ID, BadAppleApp.CODEC);
            put(StatusApp.ID, StatusApp.CODEC);
            put(HistoryApp.ID, HistoryApp.CODEC);
            put(DimensionsApp.ID, DimensionsApp.CODEC);
            put(PackageManagerApp.ID, PackageManagerApp.CODEC);
            put(WaypointsApp.ID, WaypointsApp.CODEC);
            put(DummyApp.ID, DummyApp.CODEC);
            put(LookAndFeelApp.ID, LookAndFeelApp.CODEC);
    }}.build();
    Map<Identifier, Supplier<? extends ScreenApp>> CONSTRUCTORS = new ImmutableMap.Builder<Identifier, Supplier<? extends ScreenApp>>() {{
            put(SnakeApp.ID, SnakeApp::new);
            put(ScannerApp.ID, ScannerApp::new);
            put(GpsApp.ID, GpsApp::new);
            put(BadAppleApp.ID, BadAppleApp::new);
            put(StatusApp.ID, StatusApp::new);
            put(HistoryApp.ID, HistoryApp::new);
            put(DimensionsApp.ID, DimensionsApp::new);
            put(PackageManagerApp.ID, PackageManagerApp::new);
            put(WaypointsApp.ID, WaypointsApp::new);
            put(DummyApp.ID, DummyApp::new);
            put(LookAndFeelApp.ID, LookAndFeelApp::new);
    }}.build();
    Codec<ScreenApp> CODEC = Identifier.CODEC.dispatch(ScreenApp::id, key -> ALL.getOrDefault(key, DummyApp.CODEC));

    AppView getView(TardisControl controls);

    /**
     * Draw the icon of the application to the provided canvas, the canvas provided is limited to the available area.
     * THIS IS CALLED OFF-THREAD, DON'T INTERACT WITH THE WORLD IF AT ALL POSSIBLE.
     */
    void drawIcon(TardisControl controls, ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas);

    default boolean canBeUninstalled() {
        return true;
    }

    default Text getName() {
        return Text.translatable("mini_tardis.app." + id().getNamespace() + "." + id().getPath());
    }

    Identifier id();
}
