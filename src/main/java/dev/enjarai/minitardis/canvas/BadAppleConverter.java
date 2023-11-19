//package dev.enjarai.minitardis.canvas;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPOutputStream;

// Converts initial text format to new binary format
// Can be removed
@SuppressWarnings("unused")
public class BadAppleConverter {

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
                    badApple[frame * width * height + x * height + y] = goodApple[frame][y * width + x];
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