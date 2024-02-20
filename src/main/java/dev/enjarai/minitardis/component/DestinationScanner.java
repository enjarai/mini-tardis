package dev.enjarai.minitardis.component;

import dev.enjarai.minitardis.block.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.fluid.Fluids;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.joml.Vector2i;

import java.util.Iterator;
import java.util.function.BiConsumer;

public class DestinationScanner {
    public static final int RANGE = 96;
    public static final int TOTAL_BLOCKS = RANGE * RANGE;

    private final Tardis tardis;
    private final int maxPerTick;
    private final byte[] xAxis = new byte[TOTAL_BLOCKS];
    private final byte[] zAxis = new byte[TOTAL_BLOCKS];
    private Iterator<Vector2i> xIterator = newIterator();
    private Iterator<Vector2i> zIterator = newIterator();
    private boolean shouldScanNextTick;
    private boolean isZAxis;

    public DestinationScanner(Tardis tardis, int maxPerTick) {
        this.tardis = tardis;
        this.maxPerTick = maxPerTick;
    }

    public void tick() {
        if (shouldScanNextTick) {
            tardis.getDestinationWorld().ifPresent(world -> {
                @SuppressWarnings("OptionalGetWithoutIsPresent")
                var location = tardis.getDestination().get();

                if (isZAxis) {
                    zIterator = updateAxis(world, zAxis, zIterator, (pos3, pos2) -> pos3.set(location.pos()).move(0, pos2.y, pos2.x));
                } else {
                    xIterator = updateAxis(world, xAxis, xIterator, (pos3, pos2) -> pos3.set(location.pos()).move(pos2.x, pos2.y, 0));
                }
            });

            shouldScanNextTick = false;
        } else {
            resetIterators();
        }
    }

    public byte getFor(int x, int y) {
        return getFor(getIndex(x, y));
    }

    public byte getFor(int pos) {
        return isZAxis ? zAxis[pos] : xAxis[pos];
    }

    public void useXAxis() {
        isZAxis = false;
    }

    public void useZAxis() {
        isZAxis = true;
    }

    public boolean isZAxis() {
        return isZAxis;
    }

    public void resetIterators() {
        xIterator = newIterator();
        zIterator = newIterator();
    }

    public void shouldScanNextTick() {
        shouldScanNextTick = true;
    }

    private Iterator<Vector2i> updateAxis(ServerWorld world, byte[] axis, Iterator<Vector2i> iterator, BiConsumer<BlockPos.Mutable, Vector2i> posApplier) {
        var pos3 = new BlockPos.Mutable();
        var pos2 = new Vector2i();

        for (int i = 0; i < maxPerTick; i++) {
            if (!iterator.hasNext()) return newIterator();

            pos2.set(iterator.next());
            posApplier.accept(pos3, pos2);
            pos2.add(RANGE / 2 - 1, RANGE / 2 - 1);

            var state = world.getBlockState(pos3);
            byte value = getValue(state, world, pos3);
            axis[getIndex(pos2)] = value;
        }

        return iterator;
    }

    private byte getValue(BlockState state, ServerWorld world, BlockPos pos) {
        if (state.getFluidState().isOf(Fluids.WATER)) return (byte) MapColor.BLUE.id;
        if (state.getFluidState().isOf(Fluids.LAVA)) return (byte) MapColor.ORANGE.id;
        if (state.isReplaceable()) return (byte) MapColor.BLACK.id;
        if (state.isIn(ModBlocks.TARDIS_EXTERIOR_PARTS)) return (byte) MapColor.LAPIS_BLUE.id;
        return (byte) state.getMapColor(world, pos).id;
    }

    public static int getIndex(Vector2i pos) {
        return getIndex(pos.x, pos.y);
    }

    public static int getIndex(int x, int y) {
        return (x + y * RANGE) % TOTAL_BLOCKS;
    }

    public static Vector2i getPos(int index) {
        return new Vector2i(index % RANGE - RANGE / 2, index / RANGE - RANGE / 2);
    }

    // https://stackoverflow.com/questions/3706219/algorithm-for-iterating-over-an-outward-spiral-on-a-discrete-2d-grid-from-the-or
    private static Iterator<Vector2i> newIterator() {
        return new Iterator<>() {
            // direction in which we move right now
            final Vector2i direction = new Vector2i(1, 0);
            // length of current segment
            int segment_length = 1;

            // current position and how much of current segment we passed
            final Vector2i current = new Vector2i(0, 0);
            int segment_passed = 0;
            int k = 0;

            @Override
            public boolean hasNext() {
                return k < TOTAL_BLOCKS;
            }

            @Override
            public Vector2i next() {
                k++;
                // make a step, add direction vector to current position
                current.add(direction);
                ++segment_passed;

                if (segment_passed == segment_length) {
                    // done with current segment
                    segment_passed = 0;

                    // 'rotate' directions
                    //noinspection SuspiciousNameCombination
                    direction.set(-direction.y, direction.x);

                    // increase segment length if necessary
                    if (direction.y == 0) {
                        ++segment_length;
                    }
                }

                return current;
            }
        };
    }
}
