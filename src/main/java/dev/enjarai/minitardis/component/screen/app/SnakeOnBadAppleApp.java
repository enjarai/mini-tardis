package dev.enjarai.minitardis.component.screen.app;

import com.mojang.serialization.Codec;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.ModSounds;
import dev.enjarai.minitardis.block.console.ConsoleScreenBlockEntity;
import dev.enjarai.minitardis.canvas.BadApple;
import dev.enjarai.minitardis.canvas.ModCanvasUtils;
import dev.enjarai.minitardis.component.TardisControl;
import dev.enjarai.minitardis.component.screen.element.AppleElement;
import dev.enjarai.minitardis.component.screen.element.SnakeElement;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import net.minecraft.network.packet.s2c.play.StopSoundS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ClickType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.LocalRandom;
import net.minecraft.util.math.random.Random;
import org.joml.Vector2i;

import static dev.enjarai.minitardis.component.screen.app.BadAppleApp.getCanvasColor;

public class SnakeOnBadAppleApp extends SnakeApp {
    public static final Codec<SnakeOnBadAppleApp> CODEC = Codec.unit(SnakeOnBadAppleApp::new);
    public static final Identifier ID = MiniTardis.id("bad_snake");

    @Override
    public AppView getView(TardisControl controls) {
        return new SnakeAppView(controls);
    }

    @Override
    public void drawIcon(TardisControl controls, ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        CanvasUtils.draw(canvas, 0, 0, ModCanvasUtils.BAD_SNAKE_APP);
    }

    @Override
    public Identifier id() {
        return ID;
    }

    public static class SnakeAppView extends SnakeApp.SnakeAppView {
        public SnakeAppView(TardisControl tardisControl) {
            super(tardisControl);
        }
        boolean animationStarted = false;
        int badAppleFrameCounter;

        @Override
        public void draw(ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas) {
            //if(died)return;
            var frame = MathHelper.clamp(badAppleFrameCounter, 0, BadApple.getFrameCount());

            //System.out.println(badAppleFrameCounter);
            //System.out.println(BadApple.getFrameCount());
            for (int x = 0; x < BadApple.width; x++) {
                for (int y = 0; y < BadApple.height; y++) {
                    var color = getCanvasColor(frame, x, y);
                    canvas.set((int) (x * BadApple.width / 128.0f), (int) (y * BadApple.height / 96.0f), color);
                }
            }

            badAppleFrameCounter++;

            if (badAppleFrameCounter >= BadApple.getFrameCount()) {
                reset(blockEntity);
            }

            //if(pausedTicks % 16 > 7)return;
            CanvasUtils.draw(canvas, 0, 0, ModCanvasUtils.SNAKE_OVERLAY);

            super.draw(blockEntity, canvas);
        }

        @Override
        public void screenOpen(ConsoleScreenBlockEntity blockEntity) {
            badAppleFrameCounter = Integer.MIN_VALUE; // temporary fix for apple grab beat drop sync, we go out of sync as song goes on though, gotta fix that
            var pos = blockEntity.getPos();
            //noinspection DataFlowIssue
            //blockEntity.getWorld().playSound(null, pos, ModSounds.BAD_APPLE, SoundCategory.RECORDS, 1, 1);
        }

        @Override
        public void ateApple(ConsoleScreenBlockEntity blockEntity) {
            super.ateApple(blockEntity);
            if(!this.animationStarted) {
                this.animationStarted = true;
                reset(blockEntity);
            }
        }

        public void reset(ConsoleScreenBlockEntity blockEntity) {
            badAppleFrameCounter = -5;
            StopSoundS2CPacket stopSoundS2CPacket = new StopSoundS2CPacket(ModSounds.BAD_APPLE.getId(), SoundCategory.RECORDS);

            //noinspection DataFlowIssue
            for (var player : blockEntity.getWorld().getPlayers()) {
                if (player instanceof ServerPlayerEntity serverPlayer) {
                    serverPlayer.networkHandler.sendPacket(stopSoundS2CPacket);
                }
            }

            var pos = blockEntity.getPos();
            //noinspection DataFlowIssue
            blockEntity.getWorld().playSound(null, pos, ModSounds.BAD_APPLE, SoundCategory.RECORDS, 1, 1);
        }

        @Override
        public void screenClose(ConsoleScreenBlockEntity blockEntity) {
            badAppleFrameCounter = 0;
            StopSoundS2CPacket stopSoundS2CPacket = new StopSoundS2CPacket(ModSounds.BAD_APPLE.getId(), SoundCategory.RECORDS);

            //noinspection DataFlowIssue
            for (var player : blockEntity.getWorld().getPlayers()) {
                if (player instanceof ServerPlayerEntity serverPlayer) {
                    serverPlayer.networkHandler.sendPacket(stopSoundS2CPacket);
                }
            }
        }
    }

}
