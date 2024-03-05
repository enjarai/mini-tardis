package dev.enjarai.minitardis.datagen;

import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.block.ModBlocks;
import dev.enjarai.minitardis.block.TardisExteriorExtensionBlock;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.client.*;
import net.minecraft.util.math.Direction;

import java.util.Map;

public class ModBlockStateGeneration extends FabricModelProvider {
    public static final Map<Direction, VariantSettings.Rotation> ROTATIONS = Map.of(
            Direction.NORTH, VariantSettings.Rotation.R0,
            Direction.EAST, VariantSettings.Rotation.R90,
            Direction.SOUTH, VariantSettings.Rotation.R180,
            Direction.WEST, VariantSettings.Rotation.R270
    );

    public ModBlockStateGeneration(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
        blockStateModelGenerator.blockStateCollector.accept(VariantsBlockStateSupplier.create(ModBlocks.TARDIS_EXTERIOR_EXTENSION)
                .coordinate(BlockStateVariantMap.create(TardisExteriorExtensionBlock.FACING, TardisExteriorExtensionBlock.VISIBLENESS)
                        .register((direction, integer) -> BlockStateVariant.create()
                                .put(VariantSettings.Y, ROTATIONS.get(direction))
                                .put(VariantSettings.MODEL, integer >= 16 ? MiniTardis.id("item/tardis") : MiniTardis.id("item/tardis_alpha_" + integer))
                        )
                )
        );
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {

    }
}
