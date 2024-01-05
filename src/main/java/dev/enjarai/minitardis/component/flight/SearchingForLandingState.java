package dev.enjarai.minitardis.component.flight;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.ModSounds;
import dev.enjarai.minitardis.block.ModBlocks;
import dev.enjarai.minitardis.component.Tardis;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Heightmap;

import java.util.Iterator;

public class SearchingForLandingState implements FlightState {
    public static final Codec<SearchingForLandingState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("flying_ticks").forGetter(s -> s.flyingTicks),
            Codec.INT.fieldOf("searching_ticks").forGetter(s -> s.searchingTicks),
            Codec.BOOL.fieldOf("crashing").forGetter(s -> s.crashing),
            Codec.INT.fieldOf("error_distance").forGetter(s -> s.errorDistance)
    ).apply(instance, SearchingForLandingState::new));
    public static final Identifier ID = MiniTardis.id("searching_for_landing");
    private static final int BLOCKS_PER_TICK = 256;
    private static final int MAX_SEARCH_RANGE = 24;

    int flyingTicks;
    private int searchingTicks;
    final boolean crashing;
    int errorDistance;
    private Iterator<BlockPos> searchIterator;

    private SearchingForLandingState(int flyingTicks, int searchingTicks, boolean crashing, int errorDistance) {
        this.flyingTicks = flyingTicks;
        this.searchingTicks = searchingTicks;
        this.crashing = crashing;
        this.errorDistance = errorDistance;
    }

    public SearchingForLandingState(boolean crashing, int errorDistance) {
        this(0, 0, crashing, FlyingState.trimDistance(errorDistance));
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
            var destinationWorld = destination.getWorld(tardis.getServer());

            if (searchIterator == null) {
                // Shuffle the landing location based on errorDistance
                var random = tardis.getRandom();
                destination = destination.with(destination.pos().add(random.nextBetween(-errorDistance, errorDistance), 0, random.nextBetween(-errorDistance, errorDistance)));

                var minY = destinationWorld.getBottomY();
                var maxY = minY + destinationWorld.getLogicalHeight();
                var x = destination.pos().getX();
                var z = destination.pos().getZ();
                var heightMapPos = destinationWorld
                        .getChunk(ChunkSectionPos.getSectionCoord(x), ChunkSectionPos.getSectionCoord(z))
                        .sampleHeightmap(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x & 15, z & 15);
                destination = destination.with(destination.pos().withY(
                        MathHelper.clamp(destination.pos().getY(), minY, Math.min(maxY, heightMapPos))));

                searchIterator = BlockPos.iterateOutwards(destination.pos(), MAX_SEARCH_RANGE, MAX_SEARCH_RANGE, MAX_SEARCH_RANGE).iterator();
            }

            for (int i = 0; i < BLOCKS_PER_TICK; ) {
                if (!searchIterator.hasNext()) {
                    // If we've reached the end of our iterator, we destroy/place some blocks to forcibly make a valid landing spot.
                    tardis.getControls().moderateMalfunction();

                    var random = tardis.getInteriorWorld().getRandom();
                    var location = destination.with(destination.pos()
                            .withY(MathHelper.clamp(
                                    destination.pos().getY(),
                                    destinationWorld.getBottomY() + MAX_SEARCH_RANGE + 8, // Add 8 blocks of buffer to avoid replacing bedrock
                                    destinationWorld.getBottomY() + destinationWorld.getLogicalHeight() - MAX_SEARCH_RANGE - 8
                            ))
                            .add(
                                    random.nextBetween(-MAX_SEARCH_RANGE, MAX_SEARCH_RANGE),
                                    random.nextBetween(-MAX_SEARCH_RANGE, MAX_SEARCH_RANGE),
                                    random.nextBetween(-MAX_SEARCH_RANGE, MAX_SEARCH_RANGE)
                            )
                    );
                    spawnSafetyStructure(location.getWorld(tardis.getServer()), location.pos());

                    return crashing ? new CrashingState(location) : new LandingState(location);
                }
                var pos = searchIterator.next();
                if (destinationWorld.isOutOfHeightLimit(pos)) {
                    continue;
                } else {
                    i++;
                    // Only increment i if we check a pos inside the world,
                    // this speeds up searches on the world's edge
                }

                var location = destination.with(pos);
                if (tardis.canLandAt(location)) {
                    // Only in this case will we actually land.
                    return crashing ? new CrashingState(location) : new LandingState(location);
                }
            }

            searchingTicks++;
        } else {
            tardis.getControls().minorMalfunction();
            var flyingState = new FlyingState(errorDistance);
            flyingState.errorLoops = 2;
            flyingState.flyingTicks = flyingTicks;
            return flyingState;
        }

        return this;
    }

    protected void spawnSafetyStructure(ServerWorld world, BlockPos pos) {
        for (var floorPos : BlockPos.iterate(pos.down().west().north(), pos.down().east().south())) {
            if (!world.getBlockState(floorPos).isSideSolidFullSquare(world, floorPos, Direction.UP)) {
                world.setBlockState(floorPos, ModBlocks.TARDIS_PLATING.getDefaultState());
            }
        }

        for (var roomPos : BlockPos.iterate(pos.west().north(), pos.up(2).east().south())) {
            if (!world.getBlockState(roomPos).getCollisionShape(world, roomPos).isEmpty()) {
                world.setBlockState(roomPos, Blocks.AIR.getDefaultState());
            }
        }
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
    public boolean isInteriorLightEnabled(int order) {
        if (crashing) {
            order--;
            return flyingTicks / 5 % 3 == order / 4;
        }
        return FlyingState.spinnyLighting(order, flyingTicks);
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
