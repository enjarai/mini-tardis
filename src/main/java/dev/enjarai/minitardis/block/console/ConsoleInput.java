package dev.enjarai.minitardis.block.console;

import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public interface ConsoleInput {
    default void inputSuccess(World world, BlockPos pos, SoundEvent soundEvent, float pitch) {
        if (world instanceof ServerWorld serverWorld) {
            serverWorld.playSound(null, pos, soundEvent, SoundCategory.BLOCKS, 1, pitch);
        }
    }

    default void inputFailure(World world, BlockPos pos, SoundEvent soundEvent, float pitch) {
        if (world instanceof ServerWorld serverWorld) {
            serverWorld.playSound(null, pos, soundEvent, SoundCategory.BLOCKS, 1, pitch);

            var particlePos = Vec3d.ofBottomCenter(pos);
            serverWorld.spawnParticles(
                    ParticleTypes.FIREWORK,
                    particlePos.getX(), particlePos.getY(), particlePos.getZ(),
                    10, 0, 0, 0, 0.1
            );
        }
    }
}
