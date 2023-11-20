package dev.enjarai.minitardis.block;

import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.block.console.*;
import dev.enjarai.minitardis.component.TardisControl;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.block.SimplePolymerBlock;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PointOfInterestHelper;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.Instrument;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.poi.PointOfInterestType;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class ModBlocks {
    public static final TardisExteriorBlock TARDIS_EXTERIOR =
            register("tardis_exterior", new TardisExteriorBlock(FabricBlockSettings.create()
                    .mapColor(MapColor.LAPIS_BLUE)
                    .strength(-1.0F, 3600000.0F)
                    .dropsNothing()
                    .allowsSpawning(Blocks::never)));
    public static final TardisExteriorExtensionBlock TARDIS_EXTERIOR_EXTENSION =
            register("tardis_exterior_extension", new TardisExteriorExtensionBlock(FabricBlockSettings.create()
                    .mapColor(MapColor.LAPIS_BLUE)
                    .strength(-1.0F, 3600000.0F)
                    .dropsNothing()
                    .allowsSpawning(Blocks::never)));
    public static final SimplePolymerBlock TARDIS_PLATING =
            register("tardis_plating", new SimplePolymerBlock(FabricBlockSettings.create()
                    .mapColor(MapColor.BLACK)
                    .requiresTool()
                    .strength(50.0F, 1200.0F), Blocks.DEAD_BRAIN_CORAL_BLOCK));
    public static final InteriorDoorBlock INTERIOR_DOOR =
            register("interior_door", new InteriorDoorBlock(FabricBlockSettings.create()));
    public static final ConsoleLeverBlock HANDBRAKE =
            register("handbrake", new ConsoleLeverBlock(FabricBlockSettings.create(),
                    TardisControl::handbrake));
    public static final ConsoleScreenBlock CONSOLE_SCREEN =
            register("console_screen", new ConsoleScreenBlock(FabricBlockSettings.create()));
    public static final ConsoleButtonBlock RESET_DESTINATION_BUTTON =
            register("reset_destination_button", new ConsoleButtonBlock(FabricBlockSettings.create(), Blocks.DARK_OAK_BUTTON,
                    (controls, facing) -> controls.resetDestination()));
    public static final ConsoleButtonBlock NUDGE_DESTINATION_BUTTON_1 =
            register("nudge_destination_button_1", new ConsoleButtonBlock(FabricBlockSettings.create(), Blocks.OAK_BUTTON,
                    TardisControl::nudgeDestination));
    public static final ConsoleButtonBlock NUDGE_DESTINATION_BUTTON_2 =
            register("nudge_destination_button_2", new ConsoleButtonBlock(FabricBlockSettings.create(), Blocks.SPRUCE_BUTTON,
                    TardisControl::nudgeDestination));
    public static final ConsoleRepeaterBlock COORDINATE_SCALE_SELECTOR =
            register("coordinate_scale_selector", new ConsoleRepeaterBlock(FabricBlockSettings.create(),
                    (controls, value) -> controls.updateCoordinateScale((int) Math.pow(10, value) / 10)));
    public static final ConsoleRepeaterBlock ROTATION_SELECTOR =
            register("rotation_selector", new ConsoleRepeaterBlock(FabricBlockSettings.create(),
                    (controls, value) -> controls.rotateDestination(Direction.fromHorizontal(value))));
    public static final ConsoleComparatorBlock STATE_COMPARATOR =
            register("state_comparator", new ConsoleComparatorBlock(FabricBlockSettings.create(),
                    (controls, value) -> true));
    public static final ConsoleComparatorDependentBlock VERTICAL_NUDGE_DESTINATION_BUTTON =
            register("vertical_nudge_destination_button", new ConsoleComparatorDependentBlock(FabricBlockSettings.create(),
                    (controls, value) -> controls.nudgeDestination(value ? Direction.UP : Direction.DOWN)));
    public static final ConsoleDaylightDetectorBlock FUEL_CONTROL =
            register("fuel_control", new ConsoleDaylightDetectorBlock(FabricBlockSettings.create(),
                    (controls, value) -> true)); // TODO

    public static final BlockEntityType<TardisExteriorBlockEntity> TARDIS_EXTERIOR_ENTITY =
            registerEntity("tardis_exterior", TardisExteriorBlockEntity::new, TARDIS_EXTERIOR);
    public static final BlockEntityType<ConsoleScreenBlockEntity> CONSOLE_SCREEN_ENTITY =
            registerEntity("console_screen", ConsoleScreenBlockEntity::new, CONSOLE_SCREEN);

    public static final PointOfInterestType TARDIS_EXTERIOR_POI =
            PointOfInterestHelper.register(MiniTardis.id("tardis_exterior"), 0, 1, TARDIS_EXTERIOR);
    public static final PointOfInterestType INTERIOR_DOOR_POI =
            PointOfInterestHelper.register(MiniTardis.id("interior_door"), 0, 1,
                    Arrays.stream(Direction.values()).map(d -> INTERIOR_DOOR.getDefaultState().with(InteriorDoorBlock.FACING, d)).toList());

    public static final List<? extends Block> ITEM_BLOCKS = List.of(
            TARDIS_PLATING, INTERIOR_DOOR, HANDBRAKE, CONSOLE_SCREEN,
            RESET_DESTINATION_BUTTON, NUDGE_DESTINATION_BUTTON_1, NUDGE_DESTINATION_BUTTON_2,
            COORDINATE_SCALE_SELECTOR, ROTATION_SELECTOR, STATE_COMPARATOR,
            VERTICAL_NUDGE_DESTINATION_BUTTON, FUEL_CONTROL
    );

    public static final TagKey<Block> TARDIS_EXTERIOR_PARTS =
            TagKey.of(RegistryKeys.BLOCK, MiniTardis.id("tardis_exterior_parts"));


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
