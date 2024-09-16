package dev.enjarai.minitardis.component.flight;

import dev.enjarai.minitardis.ModSounds;
import dev.enjarai.minitardis.component.Tardis;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Direction;

import java.util.Optional;
import java.util.UUID;

import static dev.enjarai.minitardis.component.flight.FlyingState.SOUND_LOOP_LENGTH;

public abstract class InterceptState implements FlightState, RespondsToFlyLever, RespondsToNudging {
    public static final int PHASES = 6;
    public static final int MAX_X_OFFSET = 3;
    public static final int MAX_Y_OFFSET = 2;

    protected final UUID otherTardis;
    protected int phasesComplete;
    protected int phaseTicks;
    int flyingTicks;
    protected int offsetX;
    protected int offsetY;

    protected InterceptState(UUID otherTardis, int flyingTicks, int phasesComplete, int phaseTicks, int offsetX, int offsetY) {
        this.otherTardis = otherTardis;
        this.flyingTicks = flyingTicks;
        this.phasesComplete = phasesComplete;
        this.phaseTicks = phaseTicks;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public InterceptState(UUID otherTardis) {
        this(otherTardis, 0, 0, 0, 0, 0);
    }

    @Override
    public void init(Tardis tardis) {
        shuffleOffsets(tardis);
    }

    public void shuffleOffsets(Tardis tardis) {
        do {
            offsetX = tardis.getRandom().nextBetween(-MAX_X_OFFSET, MAX_X_OFFSET);
            offsetY = tardis.getRandom().nextBetween(-MAX_Y_OFFSET, MAX_Y_OFFSET);
        } while (offsetX == getTargetX() && offsetY == getTargetY());
    }

    @Override
    public FlightState tick(Tardis tardis) {
        flyingTicks++;
        phaseTicks++;

        if (flyingTicks % SOUND_LOOP_LENGTH == 0) {
            float errorPitch = tardis.getInteriorWorld().getRandom().nextFloat() - 0.5f;

            playForInterior(tardis, ModSounds.TARDIS_FLY_LOOP_ERROR,
                    SoundCategory.BLOCKS, 0.6f, errorPitch);
            playForInterior(tardis, ModSounds.TARDIS_FLY_LOOP,
                    SoundCategory.BLOCKS, 0.6f, 1);
        }

        if (phasesComplete >= PHASES) {
            if (phaseTicks == 11) {
                playForInterior(tardis, SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), SoundCategory.BLOCKS, 1, 0.9f);
            } else if (phaseTicks == 16) {
                playForInterior(tardis, SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), SoundCategory.BLOCKS, 1, 1.1f);
            } else if (phaseTicks >= 21) {
                return completeMinigame(tardis);
            }
        }

        if (flyingTicks % 2 == 0 && !tardis.addOrDrainFuel(-1)) {
            tardis.getControls().moderateMalfunction();
        }

        if (tardis.getStability() <= 0) {
            return new SearchingForLandingState(true, tardis.getRandom().nextInt());
        }

        return this;
    }

    protected abstract FlightState completeMinigame(Tardis tardis);

    @Override
    public boolean toggleFlyLever(Tardis tardis, boolean active) {
        if (offsetX == getTargetX() && offsetY == getTargetY()) {
            completePhase(tardis);
            return true;
        }

        tardis.getControls().minorMalfunction();
        shuffleOffsets(tardis);
        return false;
    }

    protected void completePhase(Tardis tardis) {
        phasesComplete++;
        playForInterior(tardis, SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), SoundCategory.BLOCKS, 1, 1);

        if (phasesComplete < PHASES) {
            shuffleOffsets(tardis);
        }
    }

    @Override
    public boolean nudgeDestination(Tardis tardis, Direction direction) {
        var vec = direction.getVector();
        var shiftX = vec.getX();
        var shiftY = vec.getZ();

        offsetX = Math.clamp(offsetX + shiftX, -MAX_X_OFFSET, MAX_X_OFFSET);
        offsetY = Math.clamp(offsetY + shiftY, -MAX_Y_OFFSET, MAX_Y_OFFSET);

        return true;
    }

    public abstract int getTargetX();

    public abstract int getTargetY();

    public int getOffsetX() {
        return offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public int getPhasesComplete() {
        return phasesComplete;
    }

    public Optional<BeingInterceptedState> getLinkedState(Tardis tardis) {
        return tardis.getHolder().getTardis(otherTardis).flatMap(t -> t.getState(BeingInterceptedState.class));
    }

    @Override
    public boolean suggestTransition(Tardis tardis, FlightState newState) {
        return false;
    }

    @Override
    public float getScreenShakeIntensity(Tardis tardis) {
        return 2;
    }

    @Override
    public boolean tryChangeCourse(Tardis tardis) {
        return false;
    }

    @Override
    public boolean isSolid(Tardis tardis) {
        return false;
    }

    @Override
    public boolean isInteriorLightEnabled(int order) {
        order--;
        return flyingTicks / 5 % 2 == order / 6;
    }
}
