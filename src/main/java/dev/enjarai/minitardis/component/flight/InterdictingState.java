package dev.enjarai.minitardis.component.flight;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.component.PartialTardisLocation;
import dev.enjarai.minitardis.component.Tardis;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

import java.util.UUID;

public class InterdictingState extends InterdictState {
    public static final MapCodec<InterdictingState> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Uuids.CODEC.fieldOf("other_tardis").forGetter(s -> s.otherTardis),
            Codec.INT.fieldOf("flying_ticks").forGetter(s -> s.flyingTicks),
            Codec.INT.fieldOf("phases_complete").forGetter(s -> s.phasesComplete),
            Codec.INT.fieldOf("phase_ticks").forGetter(s -> s.phaseTicks),
            Codec.INT.fieldOf("offset_x").forGetter(s -> s.offsetX),
            Codec.INT.fieldOf("offset_y").forGetter(s -> s.offsetY)
    ).apply(instance, InterdictingState::new));
    public static final Identifier ID = MiniTardis.id("interdicting");

    protected InterdictingState(UUID otherTardis, int flyingTicks, int phasesComplete, int phaseTicks, int offsetX, int offsetY) {
        super(otherTardis, flyingTicks, phasesComplete, phaseTicks, offsetX, offsetY);
    }

    public InterdictingState(UUID otherTardis) {
        super(otherTardis);
    }

    @Override
    public FlightState tick(Tardis tardis) {
        var other = tardis.getHolder().getTardis(otherTardis);
        if (other.isEmpty()) {
            tardis.getControls().majorMalfunction();
            return new SearchingForLandingState(true, tardis.getRandom().nextInt());
        }

        if (other.get().getState(BeingInterdictedState.class).isEmpty()) {
            if (!other.get().suggestStateTransition(new BeingInterdictedState(tardis.uuid()))) {
                tardis.getControls().moderateMalfunction();
                return completeMinigame(tardis);
            }
        }

//        tardis.destabilize(1); TODO

        return super.tick(tardis);
    }

    @Override
    protected FlightState completeMinigame(Tardis tardis) {
        tardis.getControls().setDestinationLocked(true, true);
        tardis.getDestination().ifPresent(destination -> {
            tardis.setCurrentLocation(new PartialTardisLocation(destination.worldKey()));
        });
        var newState = new SearchingForLandingState(false, 0);
        newState.flyingTicks = flyingTicks;
        return newState;
    }

    @Override
    public boolean suggestTransition(Tardis tardis, FlightState newState) {
        if (newState instanceof SearchingForLandingState landingState && landingState.crashing) {
            landingState.flyingTicks = flyingTicks;
            return true;
        }
        return false;
    }

    @Override
    public int getTargetX() {
        return 0;
    }

    @Override
    public int getTargetY() {
        return 0;
    }

    @Override
    public Identifier id() {
        return ID;
    }
}
