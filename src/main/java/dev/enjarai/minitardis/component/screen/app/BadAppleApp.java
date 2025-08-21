package dev.enjarai.minitardis.component.screen.app;

import com.mojang.serialization.MapCodec;
import dev.enjarai.minitardis.ModSounds;
import dev.enjarai.minitardis.block.console.ScreenBlockEntity;
import dev.enjarai.minitardis.canvas.BadApple;
import dev.enjarai.minitardis.canvas.TardisCanvasUtils;
import dev.enjarai.minitardis.component.TardisControl;
import dev.enjarai.minitardis.component.screen.canvas.patbox.CanvasUtils;
import dev.enjarai.minitardis.component.screen.canvas.patbox.DrawableCanvas;
import net.minecraft.network.packet.s2c.play.StopSoundS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ClickType;
import net.minecraft.util.math.MathHelper;

public class BadAppleApp implements ScreenApp {
    public static final MapCodec<BadAppleApp> CODEC = MapCodec.unit(BadAppleApp::new);
    public static final short[] COLORS = new short[] {
            CanvasUtils.toLimitedColor(0x111111),
            CanvasUtils.toLimitedColor(0x333333),
            CanvasUtils.toLimitedColor(0x555555),
            CanvasUtils.toLimitedColor(0x777777),
            CanvasUtils.toLimitedColor(0x999999),
            CanvasUtils.toLimitedColor(0xbbbbbb),
            CanvasUtils.toLimitedColor(0xdddddd),
            CanvasUtils.toLimitedColor(0xffffff)
    };

    @Override
    public AppView getView(TardisControl controls) {
        return new BadAppleView();
    }

    public static short getCanvasColor(int frame, int x, int y) {
        var pixel = BadApple.getPixel(frame, x, y);
        return COLORS[pixel];
    }

    @Override
    public void drawIcon(TardisControl controls, ScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        canvas.draw(0, 0, TardisCanvasUtils.getSprite("app/bad_apple"));
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
                    canvas.setRaw((int) (x * BadApple.width / 128.0f), (int) (y * BadApple.height / 96.0f), color);
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
