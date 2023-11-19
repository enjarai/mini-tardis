package dev.enjarai.minitardis.component.screen;

import com.mojang.serialization.Codec;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.ModSounds;
import dev.enjarai.minitardis.block.console.ConsoleScreenBlockEntity;
import dev.enjarai.minitardis.canvas.BadApple;
import dev.enjarai.minitardis.component.TardisControl;
import eu.pb4.mapcanvas.api.core.CanvasColor;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ClickType;
import net.minecraft.util.Identifier;

public class BadAppleApp implements ScreenApp {
    public static final Codec<BadAppleApp> CODEC = Codec.unit(BadAppleApp::new);
    public static final Identifier ID = MiniTardis.id("bad_apple");

    @Override
    public void draw(TardisControl controls, ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        for (int x = 0; x < BadApple.width; x++) {
            for (int y = 0; y < BadApple.height; y++) {
                var pixel = BadApple.getPixel(blockEntity.badAppleFrameCounter, x, y);
                var color = switch (pixel) {
                    case 0 -> CanvasColor.BLACK_LOWEST;
                    case 1 -> CanvasColor.BLACK_LOW;
                    case 2 -> CanvasColor.BLACK_NORMAL;
                    case 3 -> CanvasColor.BLACK_HIGH;
                    case 4 -> CanvasColor.WHITE_LOWEST;
                    case 5 -> CanvasColor.WHITE_LOW;
                    case 6 -> CanvasColor.WHITE_NORMAL;
                    case 7 -> CanvasColor.WHITE_HIGH;
                    default -> CanvasColor.WHITE_HIGH;
                };
                canvas.set((int) (x * BadApple.width / 128.0f), (int) (y * BadApple.height / 96.0f), color);
            }
        }

        blockEntity.badAppleFrameCounter++;
        
        if (blockEntity.badAppleFrameCounter > BadApple.getFrameCount()) {
            blockEntity.badAppleFrameCounter = 0;
        }
    }

    @Override
    public void screenOpen(TardisControl controls, ConsoleScreenBlockEntity blockEntity) {
        blockEntity.badAppleFrameCounter = 0;
        var pos = blockEntity.getPos();
        blockEntity.getWorld().playSound(null, pos.getX(), pos.getY(), pos.getZ(), ModSounds.BAD_APPLE, SoundCategory.RECORDS, 10.0f, 1.0f);
    }

    @Override
    public void screenClose(TardisControl controls, ConsoleScreenBlockEntity blockEntity) {

    }

    @Override
    public boolean onClick(TardisControl controls, ConsoleScreenBlockEntity blockEntity, ServerPlayerEntity player, ClickType type, int x, int y) {
        return false;
    }

    @Override
    public void drawIcon(TardisControl controls, ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        CanvasUtils.fill(canvas, 0, 0, 24, 24, CanvasColor.BLACK_HIGH);
    }

    @Override
    public Identifier id() {
        return ID;
    }
}
