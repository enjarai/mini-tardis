package dev.enjarai.minitardis.component.screen;

import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.LocalRandom;
import net.minecraft.util.math.random.Random;

@SuppressWarnings("NonExtendableApiUsage")
public final class TardisScreenView implements DrawableCanvas {
    private final DrawableCanvas source;
    private final Random localRandom = new LocalRandom(0);
    private int glitchFrames;

    public TardisScreenView(DrawableCanvas source) {
        this.source = source;
    }

    public void refresh(Random random) {
        if (glitchFrames > 0 && random.nextBetween(0, 5) == 0) {
            glitchFrames = Math.max(0, glitchFrames - random.nextBetween(4, 6));
        }
    }

    public void addGlitchFrames(int frames) {
        glitchFrames += frames;
    }

    @Override
    public byte getRaw(int x, int y) {
        return source.getRaw(x, y);
    }

    @Override
    public void setRaw(int x, int y, byte color) {
        if (glitchFrames > 0) {
            localRandom.setSeed((y + glitchFrames * 999L) / 30);
            var offset1 = localRandom.nextBetween(-10, 10);
            localRandom.setSeed((y + glitchFrames * 999L) / 18);
            var offset2 = localRandom.nextBetween(-5, 5);
            source.setRaw(MathHelper.clamp(x + offset1 + offset2, 0, getWidth()), y, color);
        } else {
            source.setRaw(x, y, color);
        }
    }

    @Override
    public int getHeight() {
        return source.getHeight();
    }

    @Override
    public int getWidth() {
        return source.getWidth();
    }
}
