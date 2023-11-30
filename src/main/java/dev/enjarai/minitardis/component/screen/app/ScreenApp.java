package dev.enjarai.minitardis.component.screen.app;

import com.mojang.serialization.Codec;
import dev.enjarai.minitardis.block.console.ConsoleScreenBlockEntity;
import dev.enjarai.minitardis.component.TardisControl;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.function.Supplier;

public interface ScreenApp {
    Map<Identifier, Codec<? extends ScreenApp>> ALL = Map.of(
            SnakeApp.ID, SnakeApp.CODEC,
            ScannerApp.ID, ScannerApp.CODEC,
            GpsApp.ID, GpsApp.CODEC,
            BadAppleApp.ID, BadAppleApp.CODEC,
            StatusApp.ID, StatusApp.CODEC,
            HistoryApp.ID, HistoryApp.CODEC,
            DimensionsApp.ID, DimensionsApp.CODEC,
            PackageManagerApp.ID, PackageManagerApp.CODEC
    );
    Map<Identifier, Supplier<? extends ScreenApp>> CONSTRUCTORS = Map.of(
            SnakeApp.ID, SnakeApp::new,
            ScannerApp.ID, ScannerApp::new,
            GpsApp.ID, GpsApp::new,
            BadAppleApp.ID, BadAppleApp::new,
            StatusApp.ID, StatusApp::new,
            HistoryApp.ID, HistoryApp::new,
            DimensionsApp.ID, DimensionsApp::new,
            PackageManagerApp.ID, PackageManagerApp::new
    );
    Codec<ScreenApp> CODEC = Identifier.CODEC.dispatch(ScreenApp::id, ALL::get);

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
