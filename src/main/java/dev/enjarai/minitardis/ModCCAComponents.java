package dev.enjarai.minitardis;

import dev.enjarai.minitardis.component.ScreenShakeComponent;
import dev.enjarai.minitardis.component.TardisHolder;
import dev.enjarai.minitardis.component.TardisReference;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;
import org.ladysnake.cca.api.v3.level.LevelComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.level.LevelComponentInitializer;
import org.ladysnake.cca.api.v3.world.WorldComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.world.WorldComponentInitializer;

public class ModCCAComponents implements LevelComponentInitializer, WorldComponentInitializer, EntityComponentInitializer {
    public static final ComponentKey<TardisHolder> TARDIS_HOLDER =
            ComponentRegistry.getOrCreate(MiniTardis.id("tardis_holder"), TardisHolder.class);
    public static final ComponentKey<TardisReference> TARDIS_REFERENCE =
            ComponentRegistry.getOrCreate(MiniTardis.id("tardis_reference"), TardisReference.class);
    public static final ComponentKey<ScreenShakeComponent> SCREEN_SHAKE =
            ComponentRegistry.getOrCreate(MiniTardis.id("screen_shake"), ScreenShakeComponent.class);

    @Override
    public void registerLevelComponentFactories(LevelComponentFactoryRegistry registry) {
        registry.register(TARDIS_HOLDER, worldProperties -> new TardisHolder());
    }

    @Override
    public void registerWorldComponentFactories(WorldComponentFactoryRegistry registry) {
        registry.register(TARDIS_REFERENCE, world -> new TardisReference());
    }

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(SCREEN_SHAKE, ScreenShakeComponent::new, RespawnCopyStrategy.NEVER_COPY);
    }
}
