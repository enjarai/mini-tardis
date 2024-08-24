package dev.enjarai.minitardis.ccacomponent.screen.element;

import dev.enjarai.minitardis.block.console.ScreenBlockEntity;
import dev.enjarai.minitardis.canvas.TardisCanvasUtils;
import dev.enjarai.minitardis.ccacomponent.TardisControl;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import eu.pb4.mapcanvas.impl.view.SubView;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ClickType;
import org.jetbrains.annotations.Nullable;

public class SnakeTailElement extends PlacedElement {
    private final SnakeElement snake;
    int tickCount;
    boolean isInitialized = false;
    @Nullable
    SnakeTailElement nextSnailTail = null;

    public SnakeTailElement(SnakeElement snake) {
        super(0, 0, 4, 4);
        this.snake = snake;
    }

    @Override
    protected void drawElement(TardisControl controls, ScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        CanvasUtils.draw(canvas, 0, 0, TardisCanvasUtils.getSprite("snake_tail"));
    }

    public void drawAndPush(TardisControl controls, ScreenBlockEntity blockEntity, DrawableCanvas canvas, DrawableCanvas original) {
        if(isInitialized) this.drawElement(controls, blockEntity, canvas);
        if(nextSnailTail != null) {
            nextSnailTail.drawAndPush(
                    controls,
                    blockEntity,
                    new SubView(original, nextSnailTail.x, nextSnailTail.y, nextSnailTail.width, nextSnailTail.height),
                    original
            );
        }
    }

    public void moveToAndPush(int x, int y, int length) {
        if(nextSnailTail == null) {
            if(length < snake.tailLength) {
                nextSnailTail = new SnakeTailElement(snake);
            }
        } else {
            nextSnailTail.moveToAndPush(this.x, this.y, ++length);
        }
        moveToAndPush(x, y);
    }


    public void moveToAndPush(int x, int y) {
        this.x = x;
        this.y = y;
        if(!isInitialized)isInitialized = true;
    }



    @Override
    protected boolean onClickElement(TardisControl controls, ScreenBlockEntity blockEntity, ServerPlayerEntity player, ClickType type, int x, int y) {
        return false;
    }

    public boolean doesCollide(int x, int y) {
        if(this.snake.x == this.x && this.snake.y == this.y) return true;
        if(nextSnailTail != null) {
            return nextSnailTail.doesCollide(x, y);
        }
        return false;
    }
}
