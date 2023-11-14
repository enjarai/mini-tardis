package dev.enjarai.minitardis.component;

import com.google.common.collect.ImmutableSet;
import dev.enjarai.minitardis.MiniTardis;
import dev.onyxstudios.cca.api.v3.component.Component;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import xyz.nucleoid.fantasy.Fantasy;

import java.util.*;

public class TardisHolder implements Component {
    private MinecraftServer server;
    private Fantasy fantasy;
    private Map<UUID, Tardis> tardii = new HashMap<>();

    private void loadServer() {
        this.server = MiniTardis.getServer();
        if (this.server == null) {
            throw new IllegalStateException("Interacted with Tardis properties too early, wait for server to initialize.");
        }
        this.fantasy = Fantasy.get(server);
    }

    public MinecraftServer getServer() {
        if (server == null) {
            loadServer();
        }
        return server;
    }

    public Fantasy getFantasy() {
        if (fantasy == null) {
            loadServer();
        }
        return fantasy;
    }

    public void add(Tardis tardis) {
        tardis.holder = this;
        tardii.put(tardis.uuid(), tardis);
    }

    public Tardis get(UUID uuid) {
        return tardii.get(uuid);
    }

    public Set<Tardis> getAll() {
        return ImmutableSet.copyOf(tardii.values());
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        tardii = new HashMap<>();
        tag.getList("tardii", NbtElement.COMPOUND_TYPE).stream()
                .map(el -> Tardis.CODEC.decode(NbtOps.INSTANCE, el)
                        .getOrThrow(false, s -> {
                            throw new IllegalArgumentException(s);
                        }).getFirst()
                )
                .forEach(this::add);
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.put("tardii", Util.make(new NbtList(), li -> tardii.values().stream()
                .map(el -> Tardis.CODEC.encodeStart(NbtOps.INSTANCE, el)
                        .getOrThrow(false, s -> {
                            throw new IllegalArgumentException(s);
                        })
                ).forEach(li::add)));
    }
}
