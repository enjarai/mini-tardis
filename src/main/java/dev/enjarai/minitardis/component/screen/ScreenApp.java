package dev.enjarai.minitardis.component.screen;

import com.mojang.serialization.Codec;
import dev.enjarai.minitardis.component.TardisControl;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ClickType;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.function.Supplier;

public interface ScreenApp {
    Map<Identifier, Codec<? extends ScreenApp>> ALL = Map.of(
            ScannerApp.ID, ScannerApp.CODEC,
            GpsApp.ID, GpsApp.CODEC
    );
    Map<Identifier, Supplier<? extends ScreenApp>> CONSTRUCTORS = Map.of(
            ScannerApp.ID, ScannerApp::new,
            GpsApp.ID, GpsApp::new
    );
    Codec<ScreenApp> CODEC = Identifier.CODEC.dispatch(ScreenApp::id, ALL::get);

    void draw(TardisControl controls, DrawableCanvas canvas);

    boolean onClick(TardisControl controls, ServerPlayerEntity player, ClickType type, int x, int y);

    void drawIcon(TardisControl controls, DrawableCanvas canvas);

    Identifier id();
}
