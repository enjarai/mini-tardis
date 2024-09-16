package dev.enjarai.minitardis.component.flight;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.ModSounds;
import dev.enjarai.minitardis.component.PartialTardisLocation;
import dev.enjarai.minitardis.component.Tardis;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

import java.util.UUID;

public class BeingInterceptedState extends InterceptState {
    public static final MapCodec<BeingInterceptedState> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Uuids.CODEC.fieldOf("other_tardis").forGetter(s -> s.otherTardis),
            Codec.INT.fieldOf("flying_ticks").forGetter(s -> s.flyingTicks),
            Codec.INT.fieldOf("phases_complete").forGetter(s -> s.phasesComplete),
            Codec.INT.fieldOf("phase_ticks").forGetter(s -> s.phaseTicks),
            Codec.INT.fieldOf("offset_x").forGetter(s -> s.offsetX),
            Codec.INT.fieldOf("offset_y").forGetter(s -> s.offsetY),
            Codec.INT.fieldOf("target_x").forGetter(s -> s.targetX),
            Codec.INT.fieldOf("target_y").forGetter(s -> s.targetY)
    ).apply(instance, BeingInterceptedState::new));
    public static final Identifier ID = MiniTardis.id("being_intercepted");

    private int targetX;
    private int targetY;

    protected BeingInterceptedState(UUID otherTardis, int flyingTicks, int phasesComplete, int phaseTicks, int offsetX, int offsetY, int targetX, int targetY) {
        super(otherTardis, flyingTicks, phasesComplete, phaseTicks, offsetX, offsetY);
        this.targetX = targetX;
        this.targetY = targetY;
    }

    public BeingInterceptedState(UUID otherTardis) {
        super(otherTardis);
    }

    public void shuffleTarget(Tardis tardis) {
        targetX = tardis.getRandom().nextBetween(-MAX_X_OFFSET, MAX_X_OFFSET);
        targetY = tardis.getRandom().nextBetween(-MAX_Y_OFFSET, MAX_Y_OFFSET);
    }

    @Override
    public void init(Tardis tardis) {
        tardis.setSparksQueued(5);
    }

    @Override
    public FlightState tick(Tardis tardis) {
        if (flyingTicks % 100 == 0) {
            playForInteriorAndExterior(tardis, ModSounds.CLOISTER_BELL, SoundCategory.BLOCKS, 1, 1);
        }

        var other = tardis.getHolder().getTardis(otherTardis);
        if (other.flatMap(t -> t.getState(InterceptState.class)).isEmpty()) {
            tardis.getControls().moderateMalfunction();
            return new FlyingState(tardis.getRandom().nextInt());
        }

        return super.tick(tardis);
    }

    @Override
    protected FlightState completeMinigame(Tardis tardis) {
        var newState = new FlyingState(tardis.getRandom().nextInt());
        newState.flyingTicks = flyingTicks;
        return newState;
    }

    @Override
    protected void completePhase(Tardis tardis) {
        if (phasesComplete < PHASES) {
            shuffleTarget(tardis);
        }
        super.completePhase(tardis);
    }

    @Override
    public int getTargetX() {
        return targetX;
    }

    @Override
    public int getTargetY() {
        return targetY;
    }

    @Override
    public Identifier id() {
        return ID;
    }
}
