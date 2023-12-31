package dev.enjarai.minitardis.component;

import dev.enjarai.minitardis.MiniTardis;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.level.LevelComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.level.LevelComponentInitializer;
import dev.onyxstudios.cca.api.v3.world.WorldComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.world.WorldComponentInitializer;

public class ModComponents implements LevelComponentInitializer, WorldComponentInitializer {
    public static final ComponentKey<TardisHolder> TARDIS_HOLDER =
            ComponentRegistry.getOrCreate(MiniTardis.id("tardis_holder"), TardisHolder.class);
    public static final ComponentKey<TardisReference> TARDIS_REFERENCE =
            ComponentRegistry.getOrCreate(MiniTardis.id("tardis_reference"), TardisReference.class);

    @Override
    public void registerLevelComponentFactories(LevelComponentFactoryRegistry registry) {
        registry.register(TARDIS_HOLDER, worldProperties -> new TardisHolder());
    }

    @Override
    public void registerWorldComponentFactories(WorldComponentFactoryRegistry registry) {
        registry.register(TARDIS_REFERENCE, world -> new TardisReference());
    }
}
