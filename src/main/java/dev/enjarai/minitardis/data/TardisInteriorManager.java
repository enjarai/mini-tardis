package dev.enjarai.minitardis.data;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.component.TardisInterior;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class TardisInteriorManager extends JsonDataLoader implements IdentifiableResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private Map<Identifier, TardisInterior> interiors = Map.of();

    public TardisInteriorManager() {
        super(GSON, "mini_tardis/interiors");
    }

    @Override
    public Identifier getFabricId() {
        return MiniTardis.id("tardis_interiors");
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
        var builder = ImmutableMap.<Identifier, TardisInterior>builder();

        for (var entry : prepared.entrySet()) {
            TardisInterior.CODEC.decode(JsonOps.INSTANCE, entry.getValue()).ifSuccess(pair -> {
                 TardisInterior interior = pair.getFirst();
                 builder.put(entry.getKey(), interior);
            }).ifError(partial -> {
                MiniTardis.LOGGER.warn("Failed to load Tardis interior {}: {}", entry.getKey(), partial.message());
            });

        }

        interiors = builder.build();
        MiniTardis.LOGGER.info("Loaded {} Tardis interiors", interiors.size());
    }

    @Nullable
    public TardisInterior getInterior(Identifier id) {
        return interiors.get(id);
    }
}
