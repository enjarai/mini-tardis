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
import eu.pb4.mapcanvas.api.core.CanvasColor;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.api.font.DefaultFonts;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import net.minecraft.network.packet.s2c.play.StopSoundS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ClickType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.LocalRandom;
import net.minecraft.util.math.random.Random;
import org.joml.Vector2i;

import static java.lang.Math.abs;

public class SnakeApp implements ScreenApp {
    public static final Codec<SnakeApp> CODEC = Codec.unit(SnakeApp::new);

    @Override
    public AppView getView(TardisControl controls) {
        return new SnakeAppView(controls);
    }

    @Override
    public void drawIcon(TardisControl controls, ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        CanvasUtils.draw(canvas, 0, 0, ModCanvasUtils.SNAKE_APP);
    }

    @Override
    public ScreenAppType<?> getType() {
        return ScreenAppTypes.SNAKE;
    }

    public static class SnakeAppView implements AppView {
        public final Random deterministicRandom = new LocalRandom("LouisAndEnjaraiAreCool".hashCode());
        public final SnakeElement snake = new SnakeElement(this);
        public final AppleElement apple = new AppleElement(this);


        private final TardisControl tardisControl;
        private boolean died = false;
        private int ticksDead = 0;
        private boolean isPaused = false;
        protected int pausedTicks = 0;
        private boolean won;
        private int wonTicks = 0;

        public SnakeAppView(TardisControl tardisControl) {
            this.tardisControl = tardisControl;
        }

        @Override
        public void draw(ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas) {
            //if(died)return;
            if(pausedTicks % 16 > 7)return;
            snake.draw(tardisControl, blockEntity, canvas);
            apple.draw(tardisControl, blockEntity, canvas);
            String string = isPaused ? "Paused" : "";
            string = died ? "You died" : string;
            string = won ? "You won!" : string;
            DefaultFonts.VANILLA.drawText(canvas, string.isEmpty() ? "Score: " + snake.tailLength : string, 5, 6, 8, CanvasColor.LIGHT_GRAY_HIGH);
        }

        @Override
        public void screenTick(ConsoleScreenBlockEntity blockEntity) {
            if(won) {
                wonTicks++;
                if(wonTicks > 10)blockEntity.closeApp();
                return;
            }
            if(died) {
                ticksDead++;
                if(ticksDead > 10)blockEntity.closeApp();
                return;
            }
            if(isPaused) {
                pausedTicks++;
                return;
            }
            snake.tick(tardisControl, blockEntity);
            apple.tick(tardisControl, blockEntity);
        }

        @Override
        public boolean onClick(ConsoleScreenBlockEntity blockEntity, ServerPlayerEntity player, ClickType type, int x, int y) {
            if(this.won)return false;
            if(this.died)return false;
            if(type == ClickType.LEFT) {
                this.isPaused = !this.isPaused;
                this.pausedTicks = 0;
                return false;
            }
            Vector2i clickPos = new Vector2i(x, y);
            Vector2i snakePos = new Vector2i(snake.x, snake.y);
            Vector2i resultVector = clickPos.sub(snakePos);
            snake.snakeMove = snake.snakeMove.getSnakeMove(resultVector);
            blockEntity.getWorld().playSound(null, blockEntity.getPos(), ModSounds.SNAKE_MOVE, SoundCategory.AMBIENT, 0.3f, 1);


            return false;
        }

        @Override
        public void drawBackground(ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas) {
            CanvasUtils.draw(canvas, 0, 0, ModCanvasUtils.DIMENSIONS_BACKGROUND);
        }

        public void snakeDied(ConsoleScreenBlockEntity blockEntity) {
            blockEntity.getWorld().playSound(null, blockEntity.getPos(), ModSounds.DIE_SNAKE, SoundCategory.AMBIENT, 1f, 1);
            this.died = true;
        }

        public void won(ConsoleScreenBlockEntity blockEntity) {
            blockEntity.getWorld().playSound(null, blockEntity.getPos(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.AMBIENT, 0.3f, 1);
            this.won = true;
        }

        public void ateApple(ConsoleScreenBlockEntity blockEntity) {
            this.snake.ateApple();
            blockEntity.getWorld().playSound(null, blockEntity.getPos(), ModSounds.EAT_APPLE, SoundCategory.AMBIENT, 0.3f, 1);
        }
    }
}
