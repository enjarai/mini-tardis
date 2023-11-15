package dev.enjarai.minitardis.block;

import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.block.console.ConsoleLeverBlock;
import dev.enjarai.minitardis.block.console.ConsoleScreenBlock;
import dev.enjarai.minitardis.block.console.ConsoleScreenBlockEntity;
import dev.enjarai.minitardis.component.TardisControl;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.block.SimplePolymerBlock;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PointOfInterestHelper;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.world.poi.PointOfInterestType;

import java.util.List;

public class ModBlocks {
    public static final TardisExteriorBlock TARDIS_EXTERIOR =
            register("tardis_exterior", new TardisExteriorBlock(FabricBlockSettings.create()));
    public static final SimplePolymerBlock TARDIS_PLATING =
            register("tardis_plating", new SimplePolymerBlock(FabricBlockSettings.create(), Blocks.DEAD_BRAIN_CORAL_BLOCK));
    public static final InteriorDoorBlock INTERIOR_DOOR =
            register("interior_door", new InteriorDoorBlock(FabricBlockSettings.create()));
    public static final ConsoleLeverBlock HANDBRAKE =
            register("handbrake", new ConsoleLeverBlock(FabricBlockSettings.create(), TardisControl::handbrake));
    public static final ConsoleScreenBlock CONSOLE_SCREEN =
            register("console_screen", new ConsoleScreenBlock(FabricBlockSettings.create()));

    public static final BlockEntityType<TardisExteriorBlockEntity> TARDIS_EXTERIOR_ENTITY =
            registerEntity("tardis_exterior", TardisExteriorBlockEntity::new, TARDIS_EXTERIOR);
    public static final BlockEntityType<ConsoleScreenBlockEntity> CONSOLE_SCREEN_ENTITY =
            registerEntity("console_screen", ConsoleScreenBlockEntity::new, CONSOLE_SCREEN);

    public static final PointOfInterestType TARDIS_EXTERIOR_POI =
            PointOfInterestHelper.register(MiniTardis.id("tardis_exterior"), 0, 1, TARDIS_EXTERIOR);
    public static final PointOfInterestType INTERIOR_DOOR_POI =
            PointOfInterestHelper.register(MiniTardis.id("interior_door"), 0, 1, INTERIOR_DOOR);

    public static final List<? extends Block> ITEM_BLOCKS = List.of(TARDIS_PLATING, INTERIOR_DOOR, HANDBRAKE);


    public static void load() {
    }


    private static <T extends Block> T register(String path, T block) {
        return Registry.register(Registries.BLOCK, MiniTardis.id(path), block);
    }

    private static <T extends BlockEntity> BlockEntityType<T> registerEntity(String path, FabricBlockEntityTypeBuilder.Factory<T> factory, Block... blocks) {
        var blockEntityType = Registry.register(Registries.BLOCK_ENTITY_TYPE, MiniTardis.id(path), FabricBlockEntityTypeBuilder.create(factory, blocks).build());
        PolymerBlockUtils.registerBlockEntity(blockEntityType);
        return blockEntityType;
    }
}
