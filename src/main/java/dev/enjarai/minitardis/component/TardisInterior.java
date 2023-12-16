package dev.enjarai.minitardis.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3i;

public record TardisInterior(Identifier template, Vec3i templateCenter, Vec3i scrappyLandingPosition) {
    public static final Codec<TardisInterior> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("template").forGetter(TardisInterior::template),
            Vec3i.CODEC.optionalFieldOf("template_center", Vec3i.ZERO).forGetter(TardisInterior::templateCenter),
            Vec3i.CODEC.optionalFieldOf("scrappy_landing_position", new Vec3i(-4, 0, 4)).forGetter(TardisInterior::scrappyLandingPosition)
    ).apply(instance, TardisInterior::new));

    public StructureTemplate getStructure(StructureTemplateManager manager) {
        return manager.getTemplateOrBlank(template);
    }
}
