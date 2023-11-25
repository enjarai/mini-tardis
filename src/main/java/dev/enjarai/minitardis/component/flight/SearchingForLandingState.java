package dev.enjarai.minitardis.component.flight;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.ModSounds;
import dev.enjarai.minitardis.component.Tardis;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.Iterator;

public class SearchingForLandingState implements FlightState {
    public static final Codec<SearchingForLandingState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("flying_ticks").forGetter(s -> s.flyingTicks),
            Codec.INT.fieldOf("searching_ticks").forGetter(s -> s.searchingTicks),
            Codec.BOOL.fieldOf("crashing").forGetter(s -> s.crashing)
    ).apply(instance, SearchingForLandingState::new));
    public static final Identifier ID = MiniTardis.id("searching_for_landing");
    private static final int BLOCKS_PER_TICK = 64;
    private static final int MAX_SEARCH_RANGE = 256;

    int flyingTicks;
    private int searchingTicks;
    final boolean crashing;
    private Iterator<BlockPos> searchIterator;

    private SearchingForLandingState(int flyingTicks, int searchingTicks, boolean crashing) {
        this.flyingTicks = flyingTicks;
        this.searchingTicks = searchingTicks;
        this.crashing = crashing;
    }

    public SearchingForLandingState(boolean crashing) {
        this(0, 0, crashing);
    }

    @Override
    public FlightState tick(Tardis tardis) {
        flyingTicks++;
        if (flyingTicks % FlyingState.SOUND_LOOP_LENGTH == 0) {
            float pitch = 1;

            if (crashing) {
                pitch += tardis.getInteriorWorld().getRandom().nextFloat() - 0.5f;
            }

            playForInterior(tardis,
                    crashing ? ModSounds.TARDIS_FLY_LOOP_ERROR : ModSounds.TARDIS_FLY_LOOP,
                    SoundCategory.BLOCKS, crashing ? 1 : 0.6f, pitch);
        }

        tickScreenShake(tardis, crashing ? 2 : 0.5f);

        var maybeDestination = tardis.getDestination();
        if (maybeDestination.isPresent() && (maybeDestination.get().worldKey().equals(tardis.getExteriorWorldKey()) || crashing)) {
            var destination = crashing ? maybeDestination.get().with(tardis.getExteriorWorldKey()) : maybeDestination.get();

            if (searchIterator == null) {
                // Shuffle the landing location if we're crashing
                if (crashing) {
                    var random = tardis.getInteriorWorld().getRandom();
                    destination = destination.with(destination.pos().add(random.nextBetween(-1000, 1000), 0, random.nextBetween(-1000, 1000)));
                }

                searchIterator = BlockPos.iterateOutwards(destination.pos(), MAX_SEARCH_RANGE, MAX_SEARCH_RANGE, MAX_SEARCH_RANGE).iterator();
            }

            for (int i = 0; i < BLOCKS_PER_TICK; i++) {
                if (!searchIterator.hasNext()) {
                    // If we've reached the end of our iterator, we're mildly fucked, but let's let FlyingState handle that.
                    tardis.getControls().moderateMalfunction();
                    return new FlyingState();
                }
                var pos = searchIterator.next();

                var location = destination.with(pos);
                if (tardis.canLandAt(location)) {
                    // Only in this case will we actually land.
                    return crashing ? new CrashingState(location) : new LandingState(location);
                }
            }

            searchingTicks++;
        } else {
            tardis.getControls().minorMalfunction();
            return new FlyingState();
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
    public boolean tryChangeCourse(Tardis tardis) {
        tardis.getControls().moderateMalfunction();
        return false;
    }

    @Override
    public Text getName() {
        return crashing ? Text.translatable("mini_tardis.state.mini_tardis.searching_for_landing.crashing") : FlightState.super.getName();
    }

    @Override
    public Identifier id() {
        return ID;
    }
}
