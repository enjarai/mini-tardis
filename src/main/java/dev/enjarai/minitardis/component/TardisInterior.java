package dev.enjarai.minitardis.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.Identifier;

public record TardisInterior(Identifier template) {
    public static final Codec<TardisInterior> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("template").forGetter(TardisInterior::template)
    ).apply(instance, TardisInterior::new));

    public StructureTemplate getStructure(StructureTemplateManager manager) {
        return manager.getTemplateOrBlank(template);
    }
}
