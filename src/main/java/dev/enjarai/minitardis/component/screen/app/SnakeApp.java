package dev.enjarai.minitardis.component.screen.app;

import com.mojang.serialization.Codec;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.block.console.ConsoleScreenBlockEntity;
import dev.enjarai.minitardis.canvas.ModCanvasUtils;
import dev.enjarai.minitardis.component.TardisControl;
import dev.enjarai.minitardis.component.screen.element.AppleElement;
import dev.enjarai.minitardis.component.screen.element.DimensionStarElement;
import dev.enjarai.minitardis.component.screen.element.SnakeElement;
import eu.pb4.mapcanvas.api.core.CanvasColor;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.api.font.DefaultFonts;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ClickType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.LocalRandom;
import net.minecraft.util.math.random.Random;
import org.joml.Vector2i;

import java.util.Comparator;

import static java.lang.Math.abs;

public class SnakeApp implements ScreenApp {
    public static final Codec<SnakeApp> CODEC = Codec.unit(SnakeApp::new);
    public static final Identifier ID = MiniTardis.id("snake");

    @Override
    public AppView getView(TardisControl controls) {
        return new SnakeAppView(controls);
    }

    @Override
    public void drawIcon(TardisControl controls, ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        CanvasUtils.draw(canvas, 0, 0, ModCanvasUtils.SNAKE_APP);
    }

    @Override
    public Identifier id() {
        return ID;
    }

    public static class SnakeAppView implements AppView {
        public final SnakeElement snake = new SnakeElement(this);
        public final AppleElement apple = new AppleElement(this);

        private final TardisControl tardisControl;
        public SnakeAppView(TardisControl tardisControl) {
            this.tardisControl = tardisControl;
        }

        @Override
        public void draw(ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas) {
            snake.draw(tardisControl, blockEntity, canvas);
            apple.draw(tardisControl, blockEntity, canvas);
        }

        @Override
        public void screenTick(ConsoleScreenBlockEntity blockEntity) {
            snake.tick(tardisControl, blockEntity);
            apple.tick(tardisControl, blockEntity);
        }

        @Override
        public boolean onClick(ConsoleScreenBlockEntity blockEntity, ServerPlayerEntity player, ClickType type, int x, int y) {
            Vector2i clickPos = new Vector2i(x, y);
            Vector2i snakePos = new Vector2i(snake.x, snake.y);
            Vector2i resultVector = clickPos.sub(snakePos);
            if(abs(resultVector.x) > abs(resultVector.y)) {
                if(resultVector.x > 0)snake.setMovement(SnakeElement.SnakeMove.RIGHT);
                else snake.setMovement(SnakeElement.SnakeMove.LEFT);
            } else {
                if(resultVector.y > 0)snake.setMovement(SnakeElement.SnakeMove.UP);
                else snake.setMovement(SnakeElement.SnakeMove.DOWN);
            }
            return false;
        }

        @Override
        public void drawBackground(ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas) {
            CanvasUtils.draw(canvas, 0, 0, ModCanvasUtils.DIMENSIONS_BACKGROUND);
        }
    }

}
