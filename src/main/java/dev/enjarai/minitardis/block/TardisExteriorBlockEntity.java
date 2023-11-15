package dev.enjarai.minitardis.block;

import dev.enjarai.minitardis.component.ModComponents;
import dev.enjarai.minitardis.component.Tardis;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class TardisExteriorBlockEntity extends BlockEntity {
    private UUID tardisUuid;
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
                    .ifPresent(t -> tardis = t);
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
