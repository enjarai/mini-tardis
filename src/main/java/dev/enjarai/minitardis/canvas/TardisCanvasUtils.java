package dev.enjarai.minitardis.canvas;

import dev.enjarai.minitardis.MiniTardis;
import eu.pb4.mapcanvas.api.core.CanvasColor;
import eu.pb4.mapcanvas.api.core.CanvasImage;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.api.font.DefaultFonts;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.NoSuchElementException;

public class TardisCanvasUtils {
    private static final HashMap<Identifier, CanvasImage> cache = new HashMap<>();

    public static CanvasImage getSprite(Identifier id) {
        if (cache.containsKey(id)) {
            return cache.get(id);
        }

        CanvasImage image;
        try (var stream = Files.newInputStream(FabricLoader.getInstance().getModContainer(MiniTardis.MOD_ID).orElseThrow()
                .findPath("data/" + id.getNamespace() + "/textures/map/" + id.getPath() + ".png").orElseThrow())) {
            image = CanvasImage.from(ImageIO.read(stream));
        } catch (IOException | NoSuchElementException e) {
            MiniTardis.LOGGER.error("Failed to load canvas image " + id, e);
            image = new CanvasImage(0, 0);
        }
        cache.put(id, image);
        return image;
    }

    public static CanvasImage getSprite(String path) {
        return getSprite(MiniTardis.id(path));
    }

    public static void drawCenteredText(DrawableCanvas canvas, String text, int x, int y, CanvasColor color) {
        var width = DefaultFonts.VANILLA.getTextWidth(text, 8);
        DefaultFonts.VANILLA.drawText(canvas, text, x - width / 2, y, 8, color);
    }

    public static void load() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return MiniTardis.id("invalidate_canvas_caches");
            }

            @Override
            public void reload(ResourceManager manager) {
                cache.clear();
            }
        });
    }
}
