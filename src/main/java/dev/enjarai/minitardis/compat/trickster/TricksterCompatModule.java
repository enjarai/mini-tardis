package dev.enjarai.minitardis.compat.trickster;

import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.component.screen.app.ScreenAppType;
import net.minecraft.registry.Registry;

public class TricksterCompatModule {
    public static void load() {
        Registry.register(ScreenAppType.REGISTRY,
                MiniTardis.id("guarding_spell"),
                GuardingSpellApp.TYPE);
    }
}
