package dev.enjarai.minitardis.block;

import com.google.common.collect.ImmutableMap;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.block.console.*;
import dev.enjarai.minitardis.component.TardisControl;
import dev.enjarai.minitardis.component.flight.RefuelingState;
import dev.enjarai.minitardis.item.PolymerModels;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.block.SimplePolymerBlock;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PointOfInterestHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSetType;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Direction;
import net.minecraft.world.poi.PointOfInterestType;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static net.minecraft.block.Blocks.createLightLevelFromLitBlockState;

public class ModBlocks {
    public static final TardisExteriorBlock TARDIS_EXTERIOR =
            register("tardis_exterior", new TardisExteriorBlock(FabricBlockSettings.create()
                    .mapColor(MapColor.LAPIS_BLUE)
                    .strength(-1.0F, 3600000.0F)
                    .dropsNothing()
                    .nonOpaque()
                    .allowsSpawning(Blocks::never)));
    public static final TardisExteriorExtensionBlock TARDIS_EXTERIOR_EXTENSION =
            register("tardis_exterior_extension", new TardisExteriorExtensionBlock(FabricBlockSettings.create()
                    .mapColor(MapColor.LAPIS_BLUE)
                    .strength(-1.0F, 3600000.0F)
                    .dropsNothing()
                    .nonOpaque()
                    .allowsSpawning(Blocks::never)));
    public static final SimplePolymerBlock TARDIS_PLATING =
            register("tardis_plating", new TardisPlatingBlock(FabricBlockSettings.create()
                    .mapColor(MapColor.BLACK)
                    .requiresTool()
                    .strength(3.0F, 6.0F)));
    public static final InteriorDoorBlock INTERIOR_DOOR =
            register("interior_door", new InteriorDoorBlock(FabricBlockSettings.create()
                    .strength(3.0F)
                    .nonOpaque()));
    public static final InteriorDoorDoorsBlock INTERIOR_DOOR_DOORS =
            register("interior_door_doors", new InteriorDoorDoorsBlock(FabricBlockSettings.create()
                    .strength(3.0F)
                    .nonOpaque()));
    public static final ConsoleLeverBlock HANDBRAKE =
            register("handbrake", new ConsoleLeverBlock(FabricBlockSettings.create()
                    .noCollision()
                    .strength(0.5F)
                    .pistonBehavior(PistonBehavior.DESTROY),
                    TardisControl::handbrake, null));
    public static final ConsoleScreenBlock CONSOLE_SCREEN =
            register("console_screen", new ConsoleScreenBlock(FabricBlockSettings.create()
                    .strength(3.0F)
                    .nonOpaque()));
    public static final ConsoleButtonBlock RESET_DESTINATION_BUTTON =
            register("reset_destination_button", new ConsoleButtonBlock(FabricBlockSettings.create()
                    .noCollision()
                    .strength(0.5F)
                    .pistonBehavior(PistonBehavior.DESTROY),
                    BlockSetType.DARK_OAK, Blocks.DARK_OAK_BUTTON, true,
                    (controls, facing) -> controls.resetDestination()));
    public static final ConsoleButtonBlock NUDGE_DESTINATION_BUTTON_1 =
            register("nudge_destination_button_1", new ConsoleButtonBlock(FabricBlockSettings.create()
                    .noCollision()
                    .strength(0.5F)
                    .pistonBehavior(PistonBehavior.DESTROY),
                    BlockSetType.OAK, Blocks.OAK_BUTTON, true,
                    TardisControl::nudgeDestination));
    public static final ConsoleButtonBlock NUDGE_DESTINATION_BUTTON_2 =
            register("nudge_destination_button_2", new ConsoleButtonBlock(FabricBlockSettings.create()
                    .noCollision()
                    .strength(0.5F)
                    .pistonBehavior(PistonBehavior.DESTROY),
                    BlockSetType.SPRUCE, Blocks.SPRUCE_BUTTON, true,
                    TardisControl::nudgeDestination));
    public static final ConsoleRepeaterBlock COORDINATE_SCALE_SELECTOR =
            register("coordinate_scale_selector", new ConsoleRepeaterBlock(FabricBlockSettings.create()
                    .breakInstantly()
                    .pistonBehavior(PistonBehavior.DESTROY),
                    (controls, value) -> controls.updateCoordinateScale((int) Math.pow(10, value) / 10)));
    public static final ConsoleRepeaterBlock ROTATION_SELECTOR =
            register("rotation_selector", new ConsoleRepeaterBlock(FabricBlockSettings.create()
                    .breakInstantly()
                    .pistonBehavior(PistonBehavior.DESTROY),
                    (controls, value) -> controls.rotateDestination(Direction.fromHorizontal(value))));
    public static final ConsoleComparatorBlock STATE_COMPARATOR =
            register("state_comparator", new ConsoleComparatorBlock(FabricBlockSettings.create()
                    .breakInstantly()
                    .pistonBehavior(PistonBehavior.DESTROY),
                    (controls, value) -> true));
    public static final ConsoleComparatorDependentBlock VERTICAL_NUDGE_DESTINATION_BUTTON =
            register("vertical_nudge_destination_button", new ConsoleComparatorDependentBlock(FabricBlockSettings.create()
                    .noCollision()
                    .strength(0.5F)
                    .pistonBehavior(PistonBehavior.DESTROY),
                    (controls, value) -> controls.nudgeDestination(value ? Direction.UP : Direction.DOWN)));
    public static final ConsoleDaylightDetectorBlock FUEL_CONTROL =
            register("fuel_control", new ConsoleDaylightDetectorBlock(FabricBlockSettings.create()
                    .strength(0.2F),
                    TardisControl::setEnergyConduits));
    public static final ConsoleToggleButtonBlock COORDINATE_LOCK =
            register("coordinate_lock", new ConsoleToggleButtonBlock(FabricBlockSettings.create()
                    .noCollision()
                    .strength(0.5F)
                    .pistonBehavior(PistonBehavior.DESTROY),
                    BlockSetType.STONE, Blocks.STONE_BUTTON, false,
                    (controls, value) -> controls.setDestinationLocked(value, false)));
    public static final ConsoleLeverBlock REFUEL_TOGGLE =
            register("refuel_toggle", new ConsoleLeverBlock(FabricBlockSettings.create()
                    .noCollision()
                    .strength(0.5F)
                    .pistonBehavior(PistonBehavior.DESTROY),
                    TardisControl::refuelToggle, (controls, currentState) -> controls.getTardis().getState() instanceof RefuelingState));
    public static final InteriorLightBlock INTERIOR_LIGHT =
            register("interior_light", new InteriorLightBlock(FabricBlockSettings.create()
                    .luminance(createLightLevelFromLitBlockState(15))
                    .strength(0.3F)));
//    public static final InteriorVentBlock INTERIOR_VENT = // ඞ
//            register("interior_vent", new InteriorVentBlock(FabricBlockSettings.create()
//                    .nonOpaque()));
    public static final ConsoleCircuitryBlock POWER_COUPLING =
            register("power_coupling", new ConsoleCircuitryBlock(FabricBlockSettings.create()
                    .requiresTool()
                    .strength(3.0F, 6.0F),
                    TardisControl::toggleDisabledState));
    public static final MakeshiftEngineBlock MAKESHIFT_ENGINE =
            register("makeshift_engine", new MakeshiftEngineBlock(FabricBlockSettings.create()
                    .requiresTool()
                    .luminance(state -> 15)
                    .strength(3.0F, 6.0F)));

    public static final BlockEntityType<TardisExteriorBlockEntity> TARDIS_EXTERIOR_ENTITY =
            registerEntity("tardis_exterior", TardisExteriorBlockEntity::new, TARDIS_EXTERIOR);
    public static final BlockEntityType<ConsoleScreenBlockEntity> CONSOLE_SCREEN_ENTITY =
            registerEntity("console_screen", ConsoleScreenBlockEntity::new, CONSOLE_SCREEN);
    public static final BlockEntityType<MakeshiftEngineBlockEntity> MAKESHIFT_ENGINE_ENTITY =
            registerEntity("makeshift_engine", MakeshiftEngineBlockEntity::new, MAKESHIFT_ENGINE);

    public static final PointOfInterestType TARDIS_EXTERIOR_POI =
            PointOfInterestHelper.register(MiniTardis.id("tardis_exterior"), 0, 1, TARDIS_EXTERIOR);
    public static final PointOfInterestType INTERIOR_DOOR_POI =
            PointOfInterestHelper.register(MiniTardis.id("interior_door"), 0, 1,
                    Arrays.stream(Direction.values())
                            .filter(d -> d.getHorizontal() != -1)
                            .map(d -> INTERIOR_DOOR.getDefaultState().with(InteriorDoorBlock.FACING, d)).toList());

    public static final Map<? extends Block, Optional<PolymerModelData>> ITEM_BLOCKS;
    static {
        var builder = ImmutableMap.<Block, Optional<PolymerModelData>>builder();
        builder.put(INTERIOR_DOOR, Optional.of(PolymerModels.INTERIOR_DOOR_ITEM));
        builder.put(HANDBRAKE, Optional.empty());
        builder.put(CONSOLE_SCREEN, Optional.of(PolymerModels.ROTATING_MONITOR_PACKED));
        builder.put(RESET_DESTINATION_BUTTON, Optional.empty());
        builder.put(NUDGE_DESTINATION_BUTTON_1, Optional.empty());
        builder.put(NUDGE_DESTINATION_BUTTON_2, Optional.empty());
        builder.put(COORDINATE_SCALE_SELECTOR, Optional.empty());
        builder.put(ROTATION_SELECTOR, Optional.empty());
        builder.put(STATE_COMPARATOR, Optional.empty());
        builder.put(VERTICAL_NUDGE_DESTINATION_BUTTON, Optional.empty());
        builder.put(FUEL_CONTROL, Optional.empty());
        builder.put(COORDINATE_LOCK, Optional.empty());
        builder.put(REFUEL_TOGGLE, Optional.empty());
        builder.put(INTERIOR_LIGHT, Optional.empty());
//        builder.put(INTERIOR_VENT, Optional.empty());
        builder.put(POWER_COUPLING, Optional.empty());
        builder.put(MAKESHIFT_ENGINE, Optional.empty());
        ITEM_BLOCKS = builder.buildOrThrow();
    }

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
