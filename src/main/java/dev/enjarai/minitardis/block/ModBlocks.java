package dev.enjarai.minitardis.block;

import com.google.common.collect.ImmutableMap;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.block.console.*;
import dev.enjarai.minitardis.component.TardisControl;
import dev.enjarai.minitardis.component.flight.RefuelingState;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PointOfInterestHelper;
import net.fabricmc.fabric.api.registry.OxidizableBlocksRegistry;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.Direction;
import net.minecraft.world.poi.PointOfInterestType;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static net.minecraft.block.Blocks.createLightLevelFromLitBlockState;

public class ModBlocks {
    public static final TardisExteriorBlock TARDIS_EXTERIOR =
            register("tardis_exterior", new TardisExteriorBlock(AbstractBlock.Settings.create()
                    .mapColor(MapColor.LAPIS_BLUE)
                    .strength(-1.0F, 3600000.0F)
                    .dropsNothing()
                    .nonOpaque()
                    .blockVision(Blocks::never)
                    .allowsSpawning(Blocks::never)));
    public static final TardisExteriorExtensionBlock TARDIS_EXTERIOR_EXTENSION =
            register("tardis_exterior_extension", new TardisExteriorExtensionBlock(AbstractBlock.Settings.create()
                    .mapColor(MapColor.LAPIS_BLUE)
                    .strength(-1.0F, 3600000.0F)
                    .dropsNothing()
                    .nonOpaque()
                    .blockVision(Blocks::never)
                    .allowsSpawning(Blocks::never)));
    public static final TardisPlatingBlock TARDIS_PLATING =
            register("tardis_plating", new TardisPlatingBlock(AbstractBlock.Settings.create()
                    .mapColor(MapColor.BLACK)
                    .requiresTool()
                    .sounds(BlockSoundGroup.COPPER)
                    .strength(3.0F, 6.0F)));
    public static final InteriorDoorBlock INTERIOR_DOOR =
            register("interior_door", new InteriorDoorBlock(AbstractBlock.Settings.create()
                    .strength(3.0F)
                    .nonOpaque()));
    public static final InteriorDoorDoorsBlock INTERIOR_DOOR_DOORS =
            register("interior_door_doors", new InteriorDoorDoorsBlock(AbstractBlock.Settings.create()
                    .strength(3.0F)
                    .nonOpaque()));
    public static final ConsoleLeverBlock HANDBRAKE =
            register("handbrake", new ConsoleLeverBlock(AbstractBlock.Settings.create()
                    .noCollision()
                    .strength(0.5F)
                    .pistonBehavior(PistonBehavior.DESTROY),
                    TardisControl::handbrake, null));
    public static final ConsoleScreenBlock CONSOLE_SCREEN =
            register("console_screen", new ConsoleScreenBlock(AbstractBlock.Settings.create()
                    .strength(3.0F)
                    .nonOpaque()));
    public static final WallScreenBlock WALL_SCREEN =
            register("wall_screen", new WallScreenBlock(AbstractBlock.Settings.create()
                    .strength(3.0F)
                    .breakInstantly()
                    .nonOpaque()));
    public static final ConsoleButtonBlock RESET_DESTINATION_BUTTON =
            register("reset_destination_button", new ConsoleButtonBlock(AbstractBlock.Settings.create()
                    .noCollision()
                    .strength(0.5F)
                    .pistonBehavior(PistonBehavior.DESTROY),
                    BlockSetType.DARK_OAK,
                    (controls, facing) -> controls.resetDestination()));
    public static final ConsoleButtonBlock NUDGE_DESTINATION_BUTTON_1 =
            register("nudge_destination_button_1", new ConsoleButtonBlock(AbstractBlock.Settings.create()
                    .noCollision()
                    .strength(0.5F)
                    .pistonBehavior(PistonBehavior.DESTROY),
                    BlockSetType.OAK,
                    TardisControl::nudgeDestination));
    public static final ConsoleButtonBlock NUDGE_DESTINATION_BUTTON_2 =
            register("nudge_destination_button_2", new ConsoleButtonBlock(AbstractBlock.Settings.create()
                    .noCollision()
                    .strength(0.5F)
                    .pistonBehavior(PistonBehavior.DESTROY),
                    BlockSetType.SPRUCE,
                    TardisControl::nudgeDestination));
    public static final ConsoleRepeaterBlock COORDINATE_SCALE_SELECTOR =
            register("coordinate_scale_selector", new ConsoleRepeaterBlock(AbstractBlock.Settings.create()
                    .breakInstantly()
                    .pistonBehavior(PistonBehavior.DESTROY),
                    (controls, value) -> controls.updateCoordinateScale((int) Math.pow(10, value) / 10)));
    public static final ConsoleRepeaterBlock ROTATION_SELECTOR =
            register("rotation_selector", new ConsoleRepeaterBlock(AbstractBlock.Settings.create()
                    .breakInstantly()
                    .pistonBehavior(PistonBehavior.DESTROY),
                    (controls, value) -> controls.rotateDestination(Direction.fromHorizontal(value))));
    public static final ConsoleComparatorBlock STATE_COMPARATOR =
            register("state_comparator", new ConsoleComparatorBlock(AbstractBlock.Settings.create()
                    .breakInstantly()
                    .pistonBehavior(PistonBehavior.DESTROY),
                    (controls, value) -> true));
    public static final ConsoleComparatorDependentBlock VERTICAL_NUDGE_DESTINATION_BUTTON =
            register("vertical_nudge_destination_button", new ConsoleComparatorDependentBlock(AbstractBlock.Settings.create()
                    .noCollision()
                    .strength(0.5F)
                    .pistonBehavior(PistonBehavior.DESTROY),
                    (controls, value) -> controls.nudgeDestination(value ? Direction.UP : Direction.DOWN)));
    public static final ConsoleDaylightDetectorBlock FUEL_CONTROL =
            register("fuel_control", new ConsoleDaylightDetectorBlock(AbstractBlock.Settings.create()
                    .strength(0.2F),
                    TardisControl::setEnergyConduits));
    public static final ConsoleToggleButtonBlock COORDINATE_LOCK =
            register("coordinate_lock", new ConsoleToggleButtonBlock(AbstractBlock.Settings.create()
                    .noCollision()
                    .strength(0.5F)
                    .pistonBehavior(PistonBehavior.DESTROY),
                    BlockSetType.STONE,
                    (controls, value) -> controls.setDestinationLocked(value, false)));
    public static final ConsoleLeverBlock REFUEL_TOGGLE =
            register("refuel_toggle", new ConsoleLeverBlock(AbstractBlock.Settings.create()
                    .noCollision()
                    .strength(0.5F)
                    .pistonBehavior(PistonBehavior.DESTROY),
                    TardisControl::refuelToggle, (controls, currentState) -> controls.getTardis().getState() instanceof RefuelingState));

    public static final InteriorLightBlock INTERIOR_LIGHT =
            register("interior_light", new InteriorLightBlock(AbstractBlock.Settings.create()
                    .luminance(createLightLevelFromLitBlockState(15))
                    .strength(0.3F)
                    .requiresTool()));
    public static final OxidizableInteriorLightBlock COPPER_INTERIOR_LIGHT =
            register("copper_interior_light", new OxidizableInteriorLightBlock(AbstractBlock.Settings.create()
                    .luminance(createLightLevelFromLitBlockState(15))
                    .strength(3, 6)
                    .sounds(BlockSoundGroup.COPPER_BULB)
                    .requiresTool()
                    .solidBlock(Blocks::never),
                    Oxidizable.OxidationLevel.UNAFFECTED));
    public static final OxidizableInteriorLightBlock EXPOSED_COPPER_INTERIOR_LIGHT =
            register("exposed_copper_interior_light", new OxidizableInteriorLightBlock(AbstractBlock.Settings.copy(COPPER_INTERIOR_LIGHT)
                    .luminance(createLightLevelFromLitBlockState(12)),
                    Oxidizable.OxidationLevel.EXPOSED));
    public static final OxidizableInteriorLightBlock WEATHERED_COPPER_INTERIOR_LIGHT =
            register("weathered_copper_interior_light", new OxidizableInteriorLightBlock(AbstractBlock.Settings.copy(COPPER_INTERIOR_LIGHT)
                    .luminance(createLightLevelFromLitBlockState(8)),
                    Oxidizable.OxidationLevel.WEATHERED));
    public static final OxidizableInteriorLightBlock OXIDIZED_COPPER_INTERIOR_LIGHT =
            register("oxidized_copper_interior_light", new OxidizableInteriorLightBlock(AbstractBlock.Settings.copy(COPPER_INTERIOR_LIGHT)
                    .luminance(createLightLevelFromLitBlockState(4)),
                    Oxidizable.OxidationLevel.OXIDIZED));
    public static final InteriorLightBlock WAXED_COPPER_INTERIOR_LIGHT =
            register("waxed_copper_interior_light", new InteriorLightBlock(AbstractBlock.Settings.copy(COPPER_INTERIOR_LIGHT)));
    public static final InteriorLightBlock WAXED_EXPOSED_COPPER_INTERIOR_LIGHT =
            register("waxed_exposed_copper_interior_light", new InteriorLightBlock(AbstractBlock.Settings.copy(EXPOSED_COPPER_INTERIOR_LIGHT)));
    public static final InteriorLightBlock WAXED_WEATHERED_COPPER_INTERIOR_LIGHT =
            register("waxed_weathered_copper_interior_light", new InteriorLightBlock(AbstractBlock.Settings.copy(WEATHERED_COPPER_INTERIOR_LIGHT)));
    public static final InteriorLightBlock WAXED_OXIDIZED_COPPER_INTERIOR_LIGHT =
            register("waxed_oxidized_copper_interior_light", new InteriorLightBlock(AbstractBlock.Settings.copy(OXIDIZED_COPPER_INTERIOR_LIGHT)));

//    public static final InteriorVentBlock INTERIOR_VENT = // ඞ
//            register("interior_vent", new InteriorVentBlock(AbstractBlock.Settings.create()
//                    .nonOpaque()));
    public static final ConsoleCircuitryBlock POWER_COUPLING =
            register("power_coupling", new ConsoleCircuitryBlock(AbstractBlock.Settings.create()
                    .requiresTool()
                    .strength(3.0F, 6.0F),
                    TardisControl::toggleDisabledState));
    public static final MakeshiftEngineBlock MAKESHIFT_ENGINE =
            register("makeshift_engine", new MakeshiftEngineBlock(AbstractBlock.Settings.create()
                    .requiresTool()
                    .luminance(state -> 15)
                    .strength(3.0F, 6.0F)));

    public static final BlockEntityType<TardisExteriorBlockEntity> TARDIS_EXTERIOR_ENTITY =
            registerEntity("tardis_exterior", TardisExteriorBlockEntity::new, TARDIS_EXTERIOR);
    public static final BlockEntityType<ConsoleScreenBlockEntity> CONSOLE_SCREEN_ENTITY =
            registerEntity("console_screen", ConsoleScreenBlockEntity::new, CONSOLE_SCREEN);
    public static final BlockEntityType<WallScreenBlockEntity> WALL_SCREEN_ENTITY =
            registerEntity("wall_screen", WallScreenBlockEntity::new, WALL_SCREEN);
    public static final BlockEntityType<MakeshiftEngineBlockEntity> MAKESHIFT_ENGINE_ENTITY =
            registerEntity("makeshift_engine", MakeshiftEngineBlockEntity::new, MAKESHIFT_ENGINE);

    public static final PointOfInterestType TARDIS_EXTERIOR_POI =
            PointOfInterestHelper.register(MiniTardis.id("tardis_exterior"), 0, 1, TARDIS_EXTERIOR);
    public static final PointOfInterestType INTERIOR_DOOR_POI =
            PointOfInterestHelper.register(MiniTardis.id("interior_door"), 0, 1,
                    Arrays.stream(Direction.values())
                            .filter(d -> d.getHorizontal() != -1)
                            .map(d -> INTERIOR_DOOR.getDefaultState().with(InteriorDoorBlock.FACING, d)).toList());

    public static final Map<? extends Block, Optional<Void>> ITEM_BLOCKS;
    static {
        var builder = ImmutableMap.<Block, Optional<Void>>builder();
        builder.put(INTERIOR_DOOR, Optional.empty());
        builder.put(HANDBRAKE, Optional.empty());
        builder.put(CONSOLE_SCREEN, Optional.empty());
        builder.put(WALL_SCREEN, Optional.empty());
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
        builder.put(COPPER_INTERIOR_LIGHT, Optional.empty());
        builder.put(EXPOSED_COPPER_INTERIOR_LIGHT, Optional.empty());
        builder.put(WEATHERED_COPPER_INTERIOR_LIGHT, Optional.empty());
        builder.put(OXIDIZED_COPPER_INTERIOR_LIGHT, Optional.empty());
        builder.put(WAXED_COPPER_INTERIOR_LIGHT, Optional.empty());
        builder.put(WAXED_EXPOSED_COPPER_INTERIOR_LIGHT, Optional.empty());
        builder.put(WAXED_WEATHERED_COPPER_INTERIOR_LIGHT, Optional.empty());
        builder.put(WAXED_OXIDIZED_COPPER_INTERIOR_LIGHT, Optional.empty());
//        builder.put(INTERIOR_VENT, Optional.empty());
        builder.put(POWER_COUPLING, Optional.empty());
        builder.put(MAKESHIFT_ENGINE, Optional.empty());
        ITEM_BLOCKS = builder.buildOrThrow();
    }

    public static final TagKey<Block> TARDIS_EXTERIOR_PARTS =
            TagKey.of(RegistryKeys.BLOCK, MiniTardis.id("tardis_exterior_parts"));


    public static void load() {
        OxidizableBlocksRegistry.registerOxidizableBlockPair(COPPER_INTERIOR_LIGHT, EXPOSED_COPPER_INTERIOR_LIGHT);
        OxidizableBlocksRegistry.registerOxidizableBlockPair(EXPOSED_COPPER_INTERIOR_LIGHT, WEATHERED_COPPER_INTERIOR_LIGHT);
        OxidizableBlocksRegistry.registerOxidizableBlockPair(WEATHERED_COPPER_INTERIOR_LIGHT, OXIDIZED_COPPER_INTERIOR_LIGHT);
        OxidizableBlocksRegistry.registerWaxableBlockPair(COPPER_INTERIOR_LIGHT, WAXED_COPPER_INTERIOR_LIGHT);
        OxidizableBlocksRegistry.registerWaxableBlockPair(EXPOSED_COPPER_INTERIOR_LIGHT, WAXED_EXPOSED_COPPER_INTERIOR_LIGHT);
        OxidizableBlocksRegistry.registerWaxableBlockPair(WEATHERED_COPPER_INTERIOR_LIGHT, WAXED_WEATHERED_COPPER_INTERIOR_LIGHT);
        OxidizableBlocksRegistry.registerWaxableBlockPair(OXIDIZED_COPPER_INTERIOR_LIGHT, WAXED_OXIDIZED_COPPER_INTERIOR_LIGHT);
    }


    private static <T extends Block> T register(String path, T block) {
        return Registry.register(Registries.BLOCK, MiniTardis.id(path), block);
    }

    private static <T extends BlockEntity> BlockEntityType<T> registerEntity(String path, BlockEntityType.BlockEntityFactory<T> factory, Block... blocks) {
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, MiniTardis.id(path), BlockEntityType.Builder.create(factory, blocks).build());
    }
}
