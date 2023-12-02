package dev.enjarai.minitardis.canvas;

import dev.enjarai.minitardis.MiniTardis;
import eu.pb4.mapcanvas.api.core.CanvasColor;
import eu.pb4.mapcanvas.api.core.CanvasImage;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.api.font.DefaultFonts;
import net.fabricmc.loader.api.FabricLoader;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.file.Files;
import java.util.NoSuchElementException;

public class ModCanvasUtils {
    public static final CanvasImage SCREEN_BACKGROUND = loadImage("screen_background.png");
    public static final CanvasImage APP_BACKGROUND = loadImage("app_background.png");
    public static final CanvasImage STATUS_BACKGROUND = loadImage("status_background.png");
    public static final CanvasImage GPS_BACKGROUND = loadImage("gps_background.png");
    public static final CanvasImage HISTORY_BACKGROUND = loadImage("history_background.png");
    public static final CanvasImage DIMENSIONS_BACKGROUND = loadImage("dimensions_background.png");
    public static final CanvasImage SNAKE_OVERLAY = loadImage("snake_overlay.png");
    public static final CanvasImage PACKAGE_MANAGER_BACKGROUND = loadImage("package_manager_background.png");
    public static final CanvasImage SCREEN_SIDE_BUTTON = loadImage("screen_side_button.png");
    public static final CanvasImage SCREEN_SIDE_BUTTON_PRESSED = loadImage("screen_side_button_pressed.png");
    public static final CanvasImage COORD_WIDGET_X = loadImage("coord_widget_x.png");
    public static final CanvasImage COORD_WIDGET_Z = loadImage("coord_widget_z.png");
    public static final CanvasImage DESTINATION_FACING_WIDGET = loadImage("destination_facing_widget.png");
    public static final CanvasImage CURRENT_FACING_WIDGET = loadImage("current_facing_widget.png");
    public static final CanvasImage VERTICAL_BAR_EMPTY = loadImage("vertical_bar_empty.png");
    public static final CanvasImage VERTICAL_BAR_BLUE = loadImage("vertical_bar_blue.png");
    public static final CanvasImage VERTICAL_BAR_ORANGE = loadImage("vertical_bar_orange.png");
    public static final CanvasImage LOCK_ICON_LOCKED = loadImage("lock_icon_locked.png");
    public static final CanvasImage LOCK_ICON_UNLOCKED = loadImage("lock_icon_unlocked.png");
    public static final CanvasImage ENERGY_CONDUITS_ACTIVE = loadImage("energy_conduits_active.png");
    public static final CanvasImage ENERGY_CONDUITS_INACTIVE = loadImage("energy_conduits_inactive.png");
    public static final CanvasImage HISTORY_CURRENT_OUTLINE = loadImage("history_current_outline.png");
    public static final CanvasImage DIMENSION_MARKER = loadImage("dimension_marker.png");
    public static final CanvasImage DIMENSION_MARKER_SELECTED = loadImage("dimension_marker_selected.png");
    public static final CanvasImage SCROLL_BUTTON_UP = loadImage("scroll_button_up.png");
    public static final CanvasImage SCROLL_BUTTON_DOWN = loadImage("scroll_button_down.png");
    public static final CanvasImage SNAKE = loadImage("snake.png");
    public static final CanvasImage SNAKE_TAIL = loadImage("snake_tail.png");
    public static final CanvasImage APPLE = loadImage("apple.png");
    public static final CanvasImage APP_SELECTED = loadImage("app_selected.png");
    public static final CanvasImage SCANNER_APP = loadImage("app/scanner.png");
    public static final CanvasImage GPS_APP = loadImage("app/gps.png");
    public static final CanvasImage BAD_APPLE_APP = loadImage("app/bad_apple.png");
    public static final CanvasImage STATUS_APP = loadImage("app/status.png");
    public static final CanvasImage SNAKE_APP = loadImage("app/snake.png");
    public static final CanvasImage BAD_SNAKE_APP = loadImage("app/bad_snake.png");
    public static final CanvasImage HISTORY_APP = loadImage("app/history.png");
    public static final CanvasImage DIMENSIONS_APP = loadImage("app/dimensions.png");
    public static final CanvasImage PACKAGE_MANAGER_APP = loadImage("app/package_manager.png");

    private static CanvasImage loadImage(String filename) {
        try (var stream = Files.newInputStream(FabricLoader.getInstance().getModContainer(MiniTardis.MOD_ID).orElseThrow()
                .findPath("data/" + MiniTardis.MOD_ID + "/textures/map/" + filename).orElseThrow())) {
            return CanvasImage.from(ImageIO.read(stream));
        } catch (IOException | NoSuchElementException e) {
            MiniTardis.LOGGER.error("Failed to load canvas image " + filename, e);
            return new CanvasImage(16, 16);
        }
    }

    public static void drawCenteredText(DrawableCanvas canvas, String text, int x, int y, CanvasColor color) {
        var width = DefaultFonts.VANILLA.getTextWidth(text, 8);
        DefaultFonts.VANILLA.drawText(canvas, text, x - width / 2, y, 8, color);
    }

    public static void load() {
    }
}
