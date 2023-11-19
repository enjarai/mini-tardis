package dev.enjarai.minitardis.canvas;

import dev.enjarai.minitardis.MiniTardis;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class BadApple {
    // One-dimensional array for fewer allocations
    private static byte[] badApple;
    public static int width, height;

    static {
        // File format: GZIPed Header + Content
        // Header: width byte, height byte
        // Content: array of pixel byte values
        // pixel(frame, x, y) = badApple[frame * width * height + x * height + y]
        try (var stream = new GZIPInputStream(Files.newInputStream(FabricLoader.getInstance().getModContainer(MiniTardis.MOD_ID)
                .flatMap(container -> container.findPath("assets/" + MiniTardis.MOD_ID + "/bad_apple.bin.gz")).orElseThrow()))) {
            // stream.read() returns an int but only reads a byte
            width = stream.read();
            height = stream.read();
            badApple = stream.readAllBytes();
            System.out.println(width + " and " + height);
        } catch (IOException | NoSuchElementException e) {
            MiniTardis.LOGGER.error("Failed to load bad apple", e);
        }
    }

    public static byte getPixel(int frame, int x, int y) {
        return badApple[frame * width * height + x * height + y];
    }
}
