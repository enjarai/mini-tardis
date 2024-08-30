package dev.enjarai.minitardis.component.screen.app;

import com.mojang.serialization.Codec;
import dev.enjarai.minitardis.ModSounds;
import dev.enjarai.minitardis.block.console.ScreenBlockEntity;
import dev.enjarai.minitardis.canvas.TardisCanvasUtils;
import dev.enjarai.minitardis.component.TardisControl;
import dev.enjarai.minitardis.component.screen.canvas.CanvasColors;
import dev.enjarai.minitardis.component.screen.canvas.patbox.DrawableCanvas;
import dev.enjarai.minitardis.component.screen.canvas.patbox.font.DefaultFonts;
import dev.enjarai.minitardis.component.screen.element.AppleElement;
import dev.enjarai.minitardis.component.screen.element.SnakeElement;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ClickType;
import net.minecraft.util.math.random.LocalRandom;
import net.minecraft.util.math.random.Random;
import org.joml.Vector2i;

public class SnakeApp implements ScreenApp {
    public static final Codec<SnakeApp> CODEC = Codec.unit(SnakeApp::new);

    @Override
    public AppView getView(TardisControl controls) {
        return new SnakeAppView(controls);
    }

    @Override
    public void drawIcon(TardisControl controls, ScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        canvas.draw(0, 0, TardisCanvasUtils.getSprite("app/snake"));
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
        public void draw(ScreenBlockEntity blockEntity, DrawableCanvas canvas) {
            //if(died)return;
            if (pausedTicks % 16 > 7) return;
            snake.draw(tardisControl, blockEntity, canvas);
            apple.draw(tardisControl, blockEntity, canvas);
            String string = isPaused ? "Paused" : "";
            string = died ? "You died" : string;
            string = won ? "You won!" : string;
            DefaultFonts.VANILLA.drawText(canvas, string.isEmpty() ? "Score: " + snake.tailLength : string, 5, 6, 8, CanvasColors.LIGHT_GRAY);
        }

        @Override
        public void screenTick(ScreenBlockEntity blockEntity) {
            if (won) {
                wonTicks++;
                if (wonTicks > 10) blockEntity.closeApp();
                return;
            }
            if (died) {
                ticksDead++;
                if (ticksDead > 10) blockEntity.closeApp();
                return;
            }
            if (isPaused) {
                pausedTicks++;
                return;
            }
            snake.tick(tardisControl, blockEntity);
            apple.tick(tardisControl, blockEntity);
        }

        @Override
        public boolean onClick(ScreenBlockEntity blockEntity, ServerPlayerEntity player, ClickType type, int x, int y) {
            if (this.won) return false;
            if (this.died) return false;
            if (type == ClickType.LEFT) {
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
        public void drawBackground(ScreenBlockEntity blockEntity, DrawableCanvas canvas) {
            canvas.draw(0, 0, TardisCanvasUtils.getSprite("dimensions_background"));
        }

        public void snakeDied(ScreenBlockEntity blockEntity) {
            blockEntity.getWorld().playSound(null, blockEntity.getPos(), ModSounds.DIE_SNAKE, SoundCategory.AMBIENT, 1f, 1);
            this.died = true;
        }

        public void won(ScreenBlockEntity blockEntity) {
            blockEntity.getWorld().playSound(null, blockEntity.getPos(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.AMBIENT, 0.3f, 1);
            this.won = true;
        }

        public void ateApple(ScreenBlockEntity blockEntity) {
            this.snake.ateApple();
            blockEntity.getWorld().playSound(null, blockEntity.getPos(), ModSounds.EAT_APPLE, SoundCategory.AMBIENT, 0.3f, 1);
        }
    }
}
