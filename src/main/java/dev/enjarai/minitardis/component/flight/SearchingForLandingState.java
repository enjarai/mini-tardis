package dev.enjarai.minitardis.component.flight;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.ModSounds;
import dev.enjarai.minitardis.component.Tardis;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.Iterator;

public class SearchingForLandingState implements FlightState {
    public static final Codec<SearchingForLandingState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("flying_ticks").forGetter(s -> s.flyingTicks),
            Codec.INT.fieldOf("searching_ticks").forGetter(s -> s.searchingTicks)
    ).apply(instance, SearchingForLandingState::new));
    public static final Identifier ID = MiniTardis.id("searching_for_landing");
    private static final int BLOCKS_PER_TICK = 64;
    private static final int MAX_SEARCH_RANGE = 256;

    int flyingTicks;
    private int searchingTicks;
    private Iterator<BlockPos> searchIterator;

    private SearchingForLandingState(int flyingTicks, int searchingTicks) {
        this.flyingTicks = flyingTicks;
        this.searchingTicks = searchingTicks;
    }

    public SearchingForLandingState() {
        this(0, 0);
    }

    @Override
    public FlightState tick(Tardis tardis) {
        flyingTicks++;
        if (flyingTicks % FlyingState.SOUND_LOOP_LENGTH == 0) {
            playForInterior(tardis, ModSounds.TARDIS_FLY_LOOP, SoundCategory.BLOCKS, 0.6f, 1);
        }

        tickScreenShake(tardis, 0.5f);

        var destination = tardis.getDestination();
        if (destination.isPresent()) {
            if (searchIterator == null) {
                searchIterator = BlockPos.iterateOutwards(destination.get().pos(), MAX_SEARCH_RANGE, MAX_SEARCH_RANGE, MAX_SEARCH_RANGE).iterator();
            }

            for (int i = 0; i < BLOCKS_PER_TICK; i++) {
                if (!searchIterator.hasNext()) {
                    // If we've reached the end of our iterator, we're mildly fucked, but let's let LandingState handle that.
                    return new LandingState();
                }
                var pos = searchIterator.next();

                var location = destination.get().with(pos);
                if (tardis.canLandAt(location)) {
                    tardis.setDestination(location);
                    // Only in this case will we actually land.
                    return new LandingState();
                }
            }

            searchingTicks++;
        } else {
            // If we have no destination, the landing state will error and kick us back to a flight state.
            return new LandingState();
        }

        return this;
    }

    @Override
    public boolean suggestTransition(Tardis tardis, FlightState newState) {
        return false;
    }

    @Override
    public boolean isSolid(Tardis tardis) {
        return false;
    }

    @Override
    public boolean canChangeCourse(Tardis tardis) {
        return false;
    }

    @Override
    public Identifier id() {
        return ID;
    }
}
