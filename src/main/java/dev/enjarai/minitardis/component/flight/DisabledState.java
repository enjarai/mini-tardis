package dev.enjarai.minitardis.component.flight;

import com.mojang.serialization.Codec;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.ModSounds;
import dev.enjarai.minitardis.component.Tardis;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

public class DisabledState implements FlightState {
    public static final Codec<DisabledState> CODEC = Codec.unit(DisabledState::new);
    public static final Identifier ID = MiniTardis.id("disabled");

    @Override
    public FlightState tick(Tardis tardis) {
        return this;
    }

    @Override
    public void init(Tardis tardis) {
        playForInterior(tardis, SoundEvents.BLOCK_BEACON_DEACTIVATE, SoundCategory.BLOCKS, 1, 0);
        stopPlayingForInterior(tardis, ModSounds.CORAL_HUM);
    }

    @Override
    public void complete(Tardis tardis) {
        playForInterior(tardis, SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.BLOCKS, 1, 0);
        playForInterior(tardis, ModSounds.CORAL_HUM, SoundCategory.AMBIENT, 0.3f, 1);
    }

    @Override
    public boolean suggestTransition(Tardis tardis, FlightState newState) {
        return newState instanceof LandedState;
    }

    @Override
    public boolean isInteriorLightEnabled(int order) {
        return false;
    }

    @Override
    public boolean tryChangeCourse(Tardis tardis) {
        return false;
    }

    @Override
    public Identifier id() {
        return ID;
    }
}
