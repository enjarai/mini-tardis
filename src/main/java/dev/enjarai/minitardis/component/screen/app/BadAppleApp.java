package dev.enjarai.minitardis.component.screen.app;

import com.mojang.serialization.Codec;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.ModSounds;
import dev.enjarai.minitardis.block.console.ConsoleScreenBlockEntity;
import dev.enjarai.minitardis.canvas.BadApple;
import dev.enjarai.minitardis.canvas.ModCanvasUtils;
import dev.enjarai.minitardis.component.TardisControl;
import eu.pb4.mapcanvas.api.core.CanvasColor;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import net.minecraft.network.packet.s2c.play.StopSoundS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ClickType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class BadAppleApp implements ScreenApp {
    public static final Codec<BadAppleApp> CODEC = Codec.unit(BadAppleApp::new);
    public static final Identifier ID = MiniTardis.id("bad_apple");

    @Override
    public AppView getView(TardisControl controls, ConsoleScreenBlockEntity blockEntity) {
        return new AppView() {
            int badAppleFrameCounter;

            @Override
            public void draw(DrawableCanvas canvas) {
                var frame = MathHelper.clamp(badAppleFrameCounter, 0, BadApple.getFrameCount());
                for (int x = 0; x < BadApple.width; x++) {
                    for (int y = 0; y < BadApple.height; y++) {
                        var color = getCanvasColor(frame, x, y);
                        canvas.set((int) (x * BadApple.width / 128.0f), (int) (y * BadApple.height / 96.0f), color);
                    }
                }

                badAppleFrameCounter++;

                if (badAppleFrameCounter > BadApple.getFrameCount()) {
                    badAppleFrameCounter = 0;
                }
            }

            @Override
            public void screenOpen() {
                badAppleFrameCounter = -5; // temporary fix for apple grab beat drop sync, we go out of sync as song goes on though, gotta fix that
                var pos = blockEntity.getPos();
                //noinspection DataFlowIssue
                blockEntity.getWorld().playSound(null, pos, ModSounds.BAD_APPLE, SoundCategory.RECORDS, 1, 1);
            }

            @Override
            public void screenClose() {
                badAppleFrameCounter = 0;
                StopSoundS2CPacket stopSoundS2CPacket = new StopSoundS2CPacket(ModSounds.BAD_APPLE.getId(), SoundCategory.RECORDS);

                //noinspection DataFlowIssue
                for (var player : blockEntity.getWorld().getPlayers()) {
                    if (player instanceof ServerPlayerEntity serverPlayer) {
                        serverPlayer.networkHandler.sendPacket(stopSoundS2CPacket);
                    }
                }
            }

            @Override
            public boolean onClick(ServerPlayerEntity player, ClickType type, int x, int y) {
                return false;
            }
        };
    }

    private static CanvasColor getCanvasColor(int frame, int x, int y) {
        var pixel = BadApple.getPixel(frame, x, y);
        return switch (pixel) {
            case 0 -> CanvasColor.BLACK_LOWEST;
            case 1 -> CanvasColor.BLACK_LOW;
            case 2 -> CanvasColor.BLACK_NORMAL;
            case 3 -> CanvasColor.BLACK_HIGH;
            case 4 -> CanvasColor.WHITE_LOWEST;
            case 5 -> CanvasColor.WHITE_LOW;
            case 6 -> CanvasColor.WHITE_NORMAL;
            default -> CanvasColor.WHITE_HIGH;
        };
    }

    @Override
    public void drawIcon(TardisControl controls, ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        CanvasUtils.draw(canvas, 0, 0, ModCanvasUtils.BAD_APPLE_APP);
    }

    @Override
    public Identifier id() {
        return ID;
    }
}
