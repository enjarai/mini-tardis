package dev.enjarai.minitardis.component;

import dev.enjarai.minitardis.MiniTardis;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.level.LevelComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.level.LevelComponentInitializer;

public class ModComponents implements LevelComponentInitializer {
    public static final ComponentKey<TardisHolder> TARDIS_HOLDER =
            ComponentRegistry.getOrCreate(MiniTardis.id("tardis_holder"), TardisHolder.class);

    @Override
    public void registerLevelComponentFactories(LevelComponentFactoryRegistry registry) {
        registry.register(TARDIS_HOLDER, worldProperties -> new TardisHolder());
    }
}
