package dev.enjarai.minitardis.component.flight;

import com.mojang.serialization.Codec;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.component.Tardis;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

public class LandedState implements FlightState {
    public static final Codec<LandedState> CODEC = Codec.unit(LandedState::new);
    public static final Identifier ID = MiniTardis.id("landed");

    @Override
    public FlightState tick(Tardis tardis) {
        var world = tardis.getInteriorWorld();
        for (var player : world.getPlayers()) {
            world.playSoundFromEntity(null, player, SoundEvents.ENTITY_GUARDIAN_AMBIENT, SoundCategory.AMBIENT, 0.3f, 0f);
        }
        return this;
    }

    @Override
    public boolean suggestTransition(Tardis tardis, FlightState newState) {
        return newState instanceof TakingOffState;
    }

    @Override
    public Identifier id() {
        return ID;
    }
}
