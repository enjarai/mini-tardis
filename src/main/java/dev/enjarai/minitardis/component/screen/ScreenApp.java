package dev.enjarai.minitardis.component.screen;

import com.mojang.serialization.Codec;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.function.Supplier;

public interface ScreenApp {
    Map<Identifier, Codec<? extends ScreenApp>> ALL = Map.of(

    );
    Map<Identifier, Supplier<? extends ScreenApp>> CONSTRUCTORS = Map.of(

    );
    Codec<ScreenApp> CODEC = Identifier.CODEC.dispatch(ScreenApp::id, ALL::get);

    void draw(DrawableCanvas canvas);

    boolean onClick(int x, int y);

    Identifier id();
}
