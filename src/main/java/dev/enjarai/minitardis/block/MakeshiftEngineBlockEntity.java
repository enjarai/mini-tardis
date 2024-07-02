package dev.enjarai.minitardis.block;

import com.google.common.collect.Iterables;
import dev.enjarai.minitardis.ModSounds;
import dev.enjarai.minitardis.component.ModComponents;
import dev.enjarai.minitardis.component.Tardis;
import dev.enjarai.minitardis.component.TardisInterior;
import dev.enjarai.minitardis.component.TardisLocation;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import java.util.Set;

public class MakeshiftEngineBlockEntity extends BlockEntity {
    public static final int DEMAT_TIME = 20 * 10;

    private int dematTicks = -1;

    public MakeshiftEngineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.MAKESHIFT_ENGINE_ENTITY, pos, state);
    }

    public boolean tryHandbrake(boolean active) {
        if (dematTicks < 0) {
            if (active && isValidScrapTardis() && world != null) {
                dematTicks = 0;
                world.playSound(null, getPos(), ModSounds.TARDIS_TAKEOFF, SoundCategory.BLOCKS);
                return true;
            }
        } else {
            crash();
            return true;
        }

        return false;
    }

    public void tick(ServerWorld world, BlockPos pos, BlockState state) {
        if (dematTicks >= 0) {
            dematTicks++;

            tickParticleSphere(world, Math.min(dematTicks / 2, 100));

            if (!isValidScrapTardis()) {
                crash();
            }

            if (dematTicks >= DEMAT_TIME) {
                var tardis = createTardis(world);
                if (tardis != null) {
                    moveIntoTardis(world, tardis);
                }
                dematTicks = -1;
            }
        }
    }

    protected void tickParticleSphere(ServerWorld world, int count) {
        var random = world.getRandom();
        var centerPos = pos.toCenterPos();

        for (int i = 0; i < count; i++) {
            double horizontal = random.nextDouble() * Math.PI * 0.6;
            double vertical = random.nextDouble() * Math.PI * 2;
            double x = Math.sin(horizontal) * Math.cos(vertical) * 3;
            double y = Math.cos(horizontal) * 3;
            double z = Math.sin(horizontal) * Math.sin(vertical) * 3;

            world.spawnParticles(
                    ParticleTypes.END_ROD, centerPos.getX() + x, centerPos.getY() + y, centerPos.getZ() + z,
                    1, 0, 0, 0, 0
            );
        }
    }

    protected void crash() {
        dematTicks = -1;
        // TODO funky sounds
    }

    protected Tardis createTardis(World world) {
        if (world.getServer() == null) return null;
        var tardisHolder = ModComponents.TARDIS_HOLDER.get(world.getServer().getSaveProperties());

        var worldKey = world.getRegistryKey();
        var location = new TardisLocation(worldKey, pos, Direction.NORTH);

        return new Tardis(tardisHolder, location);
    }

    protected void moveIntoTardis(World world, Tardis tardis) {
        var tardisWorld = tardis.getInteriorWorld();
        var targetPos = Tardis.INTERIOR_CENTER.add(tardis.getInterior()
                .map(TardisInterior::scrappyLandingPosition).orElse(Vec3i.ZERO)).up();

        var blockIterator = Iterables.concat(
                BlockPos.iterate(-1, -1, -2, 1, 3, 2),
                BlockPos.iterate(2, -1, -1, 2, 3, 1),
                BlockPos.iterate(-2, -1, -1, -2, 3, 1)
        );
        // Copy blocks
        for (var blockPos : blockIterator) {
            var source = pos.add(blockPos);
            var target = targetPos.add(blockPos);

            var state = world.getBlockState(source);

            if (state.isOf(ModBlocks.MAKESHIFT_ENGINE)) {
                state = Blocks.GLASS.getDefaultState();
            }

            tardisWorld.setBlockState(target, state, Block.NOTIFY_LISTENERS);
        }
        // Delete source blocks
        for (var blockPos : blockIterator) {
            var source = pos.add(blockPos);
            world.setBlockState(source, Blocks.AIR.getDefaultState(), Block.SKIP_DROPS);
        }

        var entityBox = new Box(
                pos.down().south(2).east(2).toCenterPos(),
                pos.up(3).north(2).west(2).toCenterPos()
        );
        for (var entity : world.getEntitiesByClass(Entity.class, entityBox, e -> true)) {
            var relativePos = entity.getPos().subtract(pos.toCenterPos());
            var target = relativePos.add(targetPos.toCenterPos());

            entity.teleport(tardisWorld, target.x, target.y, target.z, Set.of(), entity.getYaw(), entity.getPitch());
        }
    }

    protected boolean isValidScrapTardis() {
        if (world == null || !world.getBlockState(pos).isOf(ModBlocks.MAKESHIFT_ENGINE)) {
            return false;
        }

        var floorPos = pos.down();
        var floorIterator = Iterables.concat(
                BlockPos.iterate(floorPos.south(2).east(), floorPos.north(2).west()),
                BlockPos.iterate(floorPos.south().east(2), floorPos.north().east(2)),
                BlockPos.iterate(floorPos.south().west(2), floorPos.north().west(2))
        );
        for (var floorTile : floorIterator) {
            if (!world.getBlockState(floorTile).isOf(ModBlocks.TARDIS_PLATING)) {
                return false;
            }
        }

        return true;
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup wrapperLookup) {
        nbt.putInt("demat_ticks", dematTicks);
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup wrapperLookup) {
        dematTicks = nbt.getInt("demat_ticks");
    }
}
