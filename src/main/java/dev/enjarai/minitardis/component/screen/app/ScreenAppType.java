package dev.enjarai.minitardis.component.screen.app;

import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import dev.enjarai.minitardis.MiniTardis;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleDefaultedRegistry;
import net.minecraft.util.Identifier;

import java.util.function.Supplier;

public record ScreenAppType<T extends ScreenApp>(MapCodec<T> mapCodec, Supplier<T> constructor, boolean spawnsAsDungeonLoot) {
    public static final RegistryKey<Registry<ScreenAppType<?>>> REGISTRY_KEY = RegistryKey.ofRegistry(MiniTardis.id("screen_app_types"));
    public static final Registry<ScreenAppType<?>> REGISTRY = new SimpleDefaultedRegistry<>(MiniTardis.id("dummy").toString(), REGISTRY_KEY, Lifecycle.stable(), false);

    public Identifier getId() {
        return REGISTRY.getId(this);
    }
}
