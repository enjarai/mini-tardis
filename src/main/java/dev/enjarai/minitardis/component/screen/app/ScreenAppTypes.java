package dev.enjarai.minitardis.component.screen.app;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.enjarai.minitardis.MiniTardis;
import net.minecraft.registry.Registry;

import java.util.function.Supplier;

public class ScreenAppTypes {
    public static final ScreenAppType<DummyApp> DUMMY = register("dummy", DummyApp.CODEC, DummyApp::new);
    public static final ScreenAppType<ScannerApp> SCANNER = register("scanner", ScannerApp.CODEC, ScannerApp::new, true);
    public static final ScreenAppType<BiomeScannerApp> BIOME_SCANNER = register("biome_scanner", BiomeScannerApp.CODEC, BiomeScannerApp::new, true);
    public static final ScreenAppType<GpsApp> GPS = register("gps", GpsApp.CODEC, GpsApp::new);
    public static final ScreenAppType<BadAppleApp> BAD_APPLE = register("bad_apple", BadAppleApp.CODEC, BadAppleApp::new, true);
    public static final ScreenAppType<StatusApp> STATUS = register("status", StatusApp.CODEC, StatusApp::new);
    public static final ScreenAppType<HistoryApp> HISTORY = register("history", HistoryApp.CODEC, HistoryApp::new);
    public static final ScreenAppType<DimensionsApp> DIMENSIONS = register("dimensions", DimensionsApp.CODEC, DimensionsApp::new, true);
    public static final ScreenAppType<PackageManagerApp> PACKAGE_MANAGER = register("package_manager", PackageManagerApp.CODEC, PackageManagerApp::new);
    public static final ScreenAppType<SnakeApp> SNAKE = register("snake", SnakeApp.CODEC, SnakeApp::new, true);
    public static final ScreenAppType<WaypointsApp> WAYPOINTS = register("waypoints", WaypointsApp.CODEC, WaypointsApp::new, true);
    public static final ScreenAppType<LookAndFeelApp> LOOK_AND_FEEL = register("look_and_feel", LookAndFeelApp.CODEC, LookAndFeelApp::new, true);
    public static final ScreenAppType<FloppyBirdApp> FLOPPY_BIRD = register("floppy_bird", FloppyBirdApp.CODEC, FloppyBirdApp::new, true);
    public static final ScreenAppType<InterdictorApp> INTERDICTOR = register("interdictor", InterdictorApp.CODEC, InterdictorApp::new, true);

    private static <T extends ScreenApp> ScreenAppType<T> register(String name, Codec<T> codec, Supplier<T> constructor, boolean spawnsAsDungeonLoot) {
        return Registry.register(ScreenAppType.REGISTRY, MiniTardis.id(name), new ScreenAppType<>(MapCodec.assumeMapUnsafe(codec), constructor, spawnsAsDungeonLoot));
    }

    private static <T extends ScreenApp> ScreenAppType<T> register(String name, Codec<T> codec, Supplier<T> constructor) {
        return register(name, codec, constructor, false);
    }

    public static void load() {
    }
}
