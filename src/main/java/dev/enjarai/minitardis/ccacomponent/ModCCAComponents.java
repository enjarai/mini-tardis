package dev.enjarai.minitardis.ccacomponent;

import dev.enjarai.minitardis.MiniTardis;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.level.LevelComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.level.LevelComponentInitializer;
import org.ladysnake.cca.api.v3.world.WorldComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.world.WorldComponentInitializer;

public class ModCCAComponents implements LevelComponentInitializer, WorldComponentInitializer {
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
