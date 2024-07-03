package dev.enjarai.minitardis.component.screen.app;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.enjarai.minitardis.ModSounds;
import dev.enjarai.minitardis.block.console.ScreenBlockEntity;
import dev.enjarai.minitardis.canvas.BadApple;
import dev.enjarai.minitardis.canvas.TardisCanvasUtils;
import dev.enjarai.minitardis.component.TardisControl;
import eu.pb4.mapcanvas.api.core.CanvasColor;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import net.minecraft.network.packet.s2c.play.StopSoundS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ClickType;
import net.minecraft.util.math.MathHelper;

public class BadAppleApp implements ScreenApp {
    public static final MapCodec<BadAppleApp> CODEC = MapCodec.unit(BadAppleApp::new);

    @Override
    public AppView getView(TardisControl controls) {
        return new BadAppleView();
    }

    public static CanvasColor getCanvasColor(int frame, int x, int y) {
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
    public void drawIcon(TardisControl controls, ScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        CanvasUtils.draw(canvas, 0, 0, TardisCanvasUtils.getSprite("app/bad_apple"));
    }

    @Override
    public ScreenAppType<?> getType() {
        return ScreenAppTypes.BAD_APPLE;
    }

    public static class BadAppleView implements AppView {
        int badAppleFrameCounter;

        @Override
        public void draw(ScreenBlockEntity blockEntity, DrawableCanvas canvas) {
            var frame = MathHelper.clamp(badAppleFrameCounter, 0, BadApple.getFrameCount());

            for (int x = 0; x < BadApple.width; x++) {
                for (int y = 0; y < BadApple.height; y++) {
                    var color = getCanvasColor(frame, x, y);
                    canvas.set((int) (x * BadApple.width / 128.0f), (int) (y * BadApple.height / 96.0f), color);
                }
            }

            badAppleFrameCounter++;

            if (badAppleFrameCounter >= BadApple.getFrameCount()) {
                this.endAnimation(blockEntity);
            }
        }

        @Override
        public void screenOpen(ScreenBlockEntity blockEntity) {
            badAppleFrameCounter = -5; // temporary fix for apple grab beat drop sync, we go out of sync as song goes on though, gotta fix that
            var pos = blockEntity.getPos();
            //noinspection DataFlowIssue
            blockEntity.getWorld().playSound(null, pos, ModSounds.BAD_APPLE, SoundCategory.RECORDS, 1, 1);
        }

        @Override
        public void screenClose(ScreenBlockEntity blockEntity) {
            badAppleFrameCounter = 0;
            StopSoundS2CPacket stopSoundS2CPacket = new StopSoundS2CPacket(ModSounds.BAD_APPLE.getId(), SoundCategory.RECORDS);

            //noinspection DataFlowIssue
            for (var player : blockEntity.getWorld().getPlayers()) {
                if (player instanceof ServerPlayerEntity serverPlayer) {
                    serverPlayer.networkHandler.sendPacket(stopSoundS2CPacket);
                }
            }
        }

        public void endAnimation(ScreenBlockEntity blockEntity) {
            blockEntity.closeApp();
        }

        @Override
        public boolean onClick(ScreenBlockEntity blockEntity, ServerPlayerEntity player, ClickType type, int x, int y) {
            return false;
        }
    };
}
