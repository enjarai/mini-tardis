package dev.enjarai.minitardis.data;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.component.TardisInterior;
import dev.enjarai.minitardis.component.arc.ArcTemplate;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class TardisArcTemplatesManager extends JsonDataLoader implements IdentifiableResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private Map<Identifier, ArcTemplate> templates = Map.of();

    public TardisArcTemplatesManager() {
        super(GSON, "mini_tardis/arc_templates");
    }

    @Override
    public Identifier getFabricId() {
        return MiniTardis.id("tardis_arc_templates");
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
        var builder = ImmutableMap.<Identifier, ArcTemplate>builder();

        for (var entry : prepared.entrySet()) {
            ArcTemplate.CODEC.decode(JsonOps.INSTANCE, entry.getValue()).get().ifLeft(pair -> {
                var template = pair.getFirst();
                builder.put(entry.getKey(), template);
            }).ifRight(partial -> {
                MiniTardis.LOGGER.warn("Failed to load Tardis ARC template {}: {}", entry.getKey(), partial.message());
            });
        }

        templates = builder.build();
        MiniTardis.LOGGER.info("Loaded {} Tardis ARC templates", templates.size());
    }

    @Nullable
    public ArcTemplate getTemplate(Identifier id) {
        return templates.get(id);
    }
}
