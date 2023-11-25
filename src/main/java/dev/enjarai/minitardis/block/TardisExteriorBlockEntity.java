package dev.enjarai.minitardis.block;

import dev.enjarai.minitardis.component.ModComponents;
import dev.enjarai.minitardis.component.Tardis;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class TardisExteriorBlockEntity extends BlockEntity {
    private UUID tardisUuid = UUID.randomUUID();
    @Nullable
    private Tardis tardis;

    public TardisExteriorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.TARDIS_EXTERIOR_ENTITY, pos, state);
    }

    public void tick(World world, BlockPos pos, BlockState state) {
        if (tardis == null) {
            world.getLevelProperties()
                    .getComponent(ModComponents.TARDIS_HOLDER)
                    .getTardis(tardisUuid)
                    .ifPresentOrElse(t -> tardis = t, () -> world.setBlockState(pos, Blocks.AIR.getDefaultState()));
        } else {
            // If the tardis isn't present at this location, we should remove this exterior block.
            if (!tardis.getCurrentLandedLocation()
                    .map(l -> l.worldKey().equals(world.getRegistryKey()) && l.pos().equals(pos))
                    .orElse(false)) {
                world.setBlockState(pos, Blocks.AIR.getDefaultState());
            }
        }
    }

    public void teleportEntityIn(Entity entity) {
        if (tardis != null) {
            tardis.teleportEntityIn(entity);
        }
    }

    public void linkTardis(Tardis tardis) {
        this.tardis = tardis;
    }

    public Tardis getLinkedTardis() {
        return tardis;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        tardisUuid = nbt.getUuid("tardis");
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        nbt.putUuid("tardis", tardis == null ? tardisUuid : tardis.uuid());
    }
}
