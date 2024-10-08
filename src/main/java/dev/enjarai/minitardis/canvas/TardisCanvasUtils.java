package dev.enjarai.minitardis.canvas;

import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.component.screen.canvas.patbox.CanvasImage;
import dev.enjarai.minitardis.component.screen.canvas.patbox.DrawableCanvas;
import dev.enjarai.minitardis.component.screen.canvas.patbox.font.DefaultFonts;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class TardisCanvasUtils {
    private static final String PATH_PREFIX = "textures/map";
    private static final CanvasImage BLANK_IMAGE = new CanvasImage(0, 0);
    private static Map<Identifier, CanvasImage> images = new HashMap<>();

    public static CanvasImage getSprite(Identifier id) {
        return images.getOrDefault(id, BLANK_IMAGE);
    }

    public static CanvasImage getSprite(String path) {
        return getSprite(MiniTardis.id(path));
    }

    public static void drawCenteredText(DrawableCanvas canvas, String text, int x, int y, short color) {
        var width = DefaultFonts.VANILLA.getTextWidth(text, 8);
        DefaultFonts.VANILLA.drawText(canvas, text, x - width / 2, y, 8, color);
    }

    public static void load() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return MiniTardis.id("canvas_images");
            }

            @Override
            public void reload(ResourceManager manager) {
                var newImages = new HashMap<Identifier, CanvasImage>();

                manager.findResources(PATH_PREFIX, id -> id.getPath().endsWith(".png")).forEach((id, resource) -> {
                    try (var stream = resource.getInputStream()) {
                        newImages.put(
                                id.withPath(path -> path.substring(PATH_PREFIX.length() + 1, path.length() - 4)),
                                CanvasImage.from(ImageIO.read(stream))
                        );
                    } catch (IOException | NoSuchElementException e) {
                        MiniTardis.LOGGER.error("Failed to load canvas image " + id, e);
                    }
                });

                images = newImages;
            }
        });
    }
}
