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
        // pixel(frame, x, y) = badApple[frame * width * height + x * width + y]
        try (var stream = new GZIPInputStream(Files.newInputStream(FabricLoader.getInstance().getModContainer(MiniTardis.MOD_ID)
                .flatMap(container -> container.findPath("assets/" + MiniTardis.MOD_ID + "/bad_apple.bin")).orElseThrow()))) {
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
        return badApple[frame * width * height + x * width + y];
    }

    // Converts initial text format to new binary format
    // Can be removed
    @SuppressWarnings("unused")
    private static class Converter {

        public static void convert(Path oldPath, Path newPath) throws IOException {
            var lines = Files.readAllLines(oldPath);

            var goodApple = new byte[lines.size()][];

            for (int i = 0; i < goodApple.length; i++) {
                var line = lines.get(i);
                var lineData = new byte[line.length()];

                for (int j = 0; j < lineData.length; j++) {
                    lineData[j] = (byte) Character.getNumericValue(line.charAt(j));
                }

                goodApple[i] = lineData;
            }

            var frames = goodApple.length;
            var width = 128;
            var height = 96;
            var badApple = new byte[frames * width * height];

            for (var frame = 0; frame < frames; frame++) {
                for (var x = 0; x < width; x++) {
                    for (var y = 0; y < height; y++) {
                        badApple[frame * width * height + x * width + y] = goodApple[frame][x * width + y];
                    }
                }
            }

            try (var stream = new GZIPOutputStream(Files.newOutputStream(newPath))) {
                stream.write(width);
                stream.write(height);
                stream.write(badApple);
            }
        }

    }
}
