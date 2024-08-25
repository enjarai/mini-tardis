package dev.enjarai.minitardis.ccacomponent.flight;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.ModSounds;
import dev.enjarai.minitardis.ccacomponent.Tardis;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

public class DisabledState implements FlightState {
    public static final MapCodec<DisabledState> CODEC = MapCodec.unit(DisabledState::new);
    public static final Identifier ID = MiniTardis.id("disabled");

    @Override
    public FlightState tick(Tardis tardis) {
        return this;
    }

    @Override
    public void init(Tardis tardis) {
        playForInterior(tardis, SoundEvents.BLOCK_RESPAWN_ANCHOR_DEPLETE.value(), SoundCategory.BLOCKS, 2, 0);
        playForInterior(tardis, SoundEvents.BLOCK_BEACON_DEACTIVATE, SoundCategory.BLOCKS, 2, 0);
        stopPlayingForInterior(tardis, ModSounds.CORAL_HUM);
    }

    @Override
    public boolean suggestTransition(Tardis tardis, FlightState newState) {
        return newState instanceof BootingUpState;
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
    public boolean isPowered(Tardis tardis) {
        return false;
    }

    @Override
    public Identifier id() {
        return ID;
    }
}
