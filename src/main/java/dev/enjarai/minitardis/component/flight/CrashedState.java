package dev.enjarai.minitardis.component.flight;

import com.mojang.serialization.Codec;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.ModSounds;
import dev.enjarai.minitardis.component.Tardis;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

public class CrashedState implements FlightState {
    public static final Codec<CrashedState> CODEC = Codec.unit(CrashedState::new);
    public static final Identifier ID = MiniTardis.id("crashed");

    private int litLamps;

    @Override
    public FlightState tick(Tardis tardis) {
        if (tardis.getInteriorWorld().getTime() % 100 == 0) {
            playForInteriorAndExterior(tardis, ModSounds.CLOISTER_BELL, SoundCategory.BLOCKS, 1, 1);
        }

        if (tardis.getInteriorWorld().getRandom().nextInt(20) == 0) {
            var size = tardis.getInteriorWorld().getRandom().nextInt(6);
            var shift = tardis.getInteriorWorld().getRandom().nextInt(24);
            litLamps = Integer.MAX_VALUE >> (32 - size) << shift;
        }

        return this;
    }

    @Override
    public void init(Tardis tardis) {
        stopPlayingForInterior(tardis, ModSounds.CORAL_HUM);
    }

    @Override
    public boolean suggestTransition(Tardis tardis, FlightState newState) {
        return newState instanceof DisabledState;
    }

    @Override
    public boolean isInteriorLightEnabled(int order) {
        return (litLamps & (1 << order)) > 0;
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
