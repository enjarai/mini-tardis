package dev.enjarai.minitardis.component;

import java.util.Iterator;
import java.util.function.BiConsumer;

import org.joml.Vector2i;

import dev.enjarai.minitardis.block.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.block.MapColor.Brightness;
import net.minecraft.fluid.Fluids;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class BiomeScanner {
	public static final int RANGE = 9216;
    public static final int TOTAL_BLOCKS = RANGE * RANGE;

    private final int maxPerTick;
    private final byte[] biomeMap = new byte[TOTAL_BLOCKS];
    private Iterator<Vector2i> iterator = newIterator();
    private boolean shouldScanNextTick;
    private boolean isZAxis;

    public BiomeScanner(int maxPerTick) {
        this.maxPerTick = maxPerTick;
    }

    public void tick() {
        if (shouldScanNextTick) {
            tardis.getDestinationWorld().ifPresent(world -> {
                @SuppressWarnings("OptionalGetWithoutIsPresent")
                var location = tardis.getDestination().get();

                iterator = update(world, biomeMap, iterator, (pos3, pos2) -> pos3.set(location.pos()).move(pos2.x, 0, pos2.y));
            });

            shouldScanNextTick = false;
        } else {
        	iterator = newIterator();
        }
    }

    public byte getFor(int x, int y) {
        return getFor(getIndex(x, y));
    }

    public byte getFor(int pos) {
        return biomeMap[pos];
    }

    public void shouldScanNextTick() {
        shouldScanNextTick = true;
    }

    private Iterator<Vector2i> update(ServerWorld world, byte[] map, Iterator<Vector2i> iterator, BiConsumer<BlockPos.Mutable, Vector2i> posApplier) {
        var pos3 = new BlockPos.Mutable();
        var pos2 = new Vector2i();

        for (int i = 0; i < maxPerTick; i++) {
            if (!iterator.hasNext()) return newIterator();

            pos2.set(iterator.next());
            posApplier.accept(pos3, pos2);
            pos2.add(RANGE / 2 - 1, RANGE / 2 - 1);

            byte value = getValue(world, pos3);
            map[getIndex(pos2)] = value;
        }

        return iterator;
    }

    private byte getValue(ServerWorld world, BlockPos pos) {
    	switch (world.getBiome(pos).getKey().get().getValue().toTranslationKey()) {
    		case "biome.minecraft.ocean":
    			return (byte) MapColor.DARK_AQUA.id;
    		default:
    			return (byte) MapColor.CYAN.id;
    	}
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
