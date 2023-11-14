package dev.enjarai.minitardis.component;

import dev.onyxstudios.cca.api.v3.component.Component;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class TardisReference implements Component {
    @Nullable
    Tardis tardis;

    public Optional<Tardis> getTardis() {
        return Optional.ofNullable(tardis);
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        // No persistence required
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        // Not here either
    }
}
