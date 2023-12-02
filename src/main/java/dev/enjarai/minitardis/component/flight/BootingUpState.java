package dev.enjarai.minitardis.component.flight;

import com.mojang.serialization.Codec;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.ModSounds;
import dev.enjarai.minitardis.component.HistoryEntry;
import dev.enjarai.minitardis.component.PartialTardisLocation;
import dev.enjarai.minitardis.component.Tardis;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

public class BootingUpState extends TransitionalFlightState {
    public static final Codec<BootingUpState> CODEC = Codec.INT
            .xmap(BootingUpState::new, s -> s.ticksPassed).fieldOf("ticks_passed").codec();
    public static final Identifier ID = MiniTardis.id("booting_up");

    private BootingUpState(int ticksPassed) {
        super(ticksPassed);
    }

    public BootingUpState() {
        this(0);
    }

    @Override
    public void complete(Tardis tardis) {
        playForInterior(tardis, SoundEvents.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, SoundCategory.BLOCKS, 2, 0);
        playForInterior(tardis, SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.BLOCKS, 2, 0);
        playForInterior(tardis, ModSounds.CORAL_HUM, SoundCategory.AMBIENT, 0.3f, 1);
    }

    @Override
    public FlightState tick(Tardis tardis) {
        if (ticksPassed % 15 == 0) {
            playForInterior(tardis, SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, SoundCategory.BLOCKS, 1, ticksPassed / 100f);
        }

        return super.tick(tardis);
    }

    @Override
    public boolean suggestTransition(Tardis tardis, FlightState newState) {
        return false;
    }

    @Override
    public FlightState getNextState(Tardis tardis) {
        return new LandedState();
    }

    @Override
    public int getTransitionDuration(Tardis tardis) {
        return 15 * 12;
    }

    @Override
    public boolean isInteriorLightEnabled(int order) {
        return order <= ticksPassed / 15;
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
