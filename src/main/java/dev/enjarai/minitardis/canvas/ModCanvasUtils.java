package dev.enjarai.minitardis.canvas;

import dev.enjarai.minitardis.MiniTardis;
import eu.pb4.mapcanvas.api.core.CanvasImage;
import net.fabricmc.loader.api.FabricLoader;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.file.Files;
import java.util.NoSuchElementException;

public class ModCanvasUtils {
    public static final CanvasImage SCREEN_BACKGROUND = loadImage("screen_background.png");
    public static final CanvasImage APP_BACKGROUND = loadImage("app_background.png");
    public static final CanvasImage SCREEN_SIDE_BUTTON = loadImage("screen_side_button.png");
    public static final CanvasImage SCREEN_SIDE_BUTTON_PRESSED = loadImage("screen_side_button_pressed.png");
    public static final CanvasImage COORD_WIDGET = loadImage("coord_widget.png");
    public static final CanvasImage FACING_WIDGET = loadImage("facing_widget.png");

    private static CanvasImage loadImage(String filename) {
        try (var stream = Files.newInputStream(FabricLoader.getInstance().getModContainer(MiniTardis.MOD_ID).orElseThrow()
                .findPath("assets/" + MiniTardis.MOD_ID + "/textures/map/" + filename).orElseThrow())) {
            return CanvasImage.from(ImageIO.read(stream));
        } catch (IOException | NoSuchElementException e) {
            MiniTardis.LOGGER.error("Failed to load canvas image " + filename, e);
            return new CanvasImage(16, 16);
        }
    }

    public static void load() {
    }
}
