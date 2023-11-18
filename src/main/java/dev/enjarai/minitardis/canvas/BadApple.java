package dev.enjarai.minitardis.canvas;

import dev.enjarai.minitardis.MiniTardis;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.util.NoSuchElementException;

public class BadApple {
    private static byte[][] badApple = new byte[0][];

    static {
        try {
            var lines = Files.readAllLines(FabricLoader.getInstance().getModContainer(MiniTardis.MOD_ID).orElseThrow()
                    .findPath("assets/" + MiniTardis.MOD_ID + "/bad_apple.txt").orElseThrow());

            badApple = new byte[lines.size()][];

            for (int i = 0; i < badApple.length; i++) {
                var line = lines.get(i);
                var lineData = new byte[line.length() - 1];

                for (int j = 0; j < lineData.length; j++) {
                    lineData[j] = (byte) Character.getNumericValue(line.charAt(j));
                }

                badApple[i] = lineData;
            }
        } catch (IOException | NoSuchElementException e) {
            MiniTardis.LOGGER.error("Failed to load bad apple", e);
        }
    }

    public static byte getPixel(int frame, int pixel) {
        return badApple[frame][pixel];
    }
}
