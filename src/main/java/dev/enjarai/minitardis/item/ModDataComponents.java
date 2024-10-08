package dev.enjarai.minitardis.item;

import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.component.screen.app.ScreenApp;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.function.UnaryOperator;

public class ModDataComponents {

    public static final ComponentType<List<ScreenApp>> APP = register(
            "app", builder -> builder.codec(ScreenApp.CODEC.listOf()).cache()
    );

    private static <T> ComponentType<T> register(String id, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
        return Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(MiniTardis.MOD_ID, id), builderOperator.apply(ComponentType.builder()).build());
    }

    public static void init() {

    }
}
