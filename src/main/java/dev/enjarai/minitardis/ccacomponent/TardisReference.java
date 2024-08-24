package dev.enjarai.minitardis.ccacomponent;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.Component;

import java.util.Optional;

public class TardisReference implements Component {
    @Nullable
    Tardis tardis;

    public Optional<Tardis> getTardis() {
        return Optional.ofNullable(tardis);
    }

    @Override
    public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        // No persistence required
    }

    @Override
    public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        // Not here either
    }
}
