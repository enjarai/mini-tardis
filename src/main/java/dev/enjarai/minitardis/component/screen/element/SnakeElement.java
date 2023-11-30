package dev.enjarai.minitardis.component.screen.element;

import dev.enjarai.minitardis.block.console.ConsoleScreenBlockEntity;
import dev.enjarai.minitardis.canvas.ModCanvasUtils;
import dev.enjarai.minitardis.component.TardisControl;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ClickType;

public class SnakeElement extends PlacedElement {
    int tickCount;
    private SnakeMove snakeMove = SnakeMove.RIGHT;

    public SnakeElement() {
        super(2, 18, 4, 4);
    }

    @Override
    protected void drawElement(TardisControl controls, ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        CanvasUtils.draw(canvas, 0, 0, ModCanvasUtils.SNAKE);
    }

    @Override
    protected boolean onClickElement(TardisControl controls, ConsoleScreenBlockEntity blockEntity, ServerPlayerEntity player, ClickType type, int x, int y) {
        return false;
    }

    @Override
    public void tick(TardisControl controls, ConsoleScreenBlockEntity blockEntity) {
        tickCount++;
        if (tickCount % 4 == 0) {
            move(this.snakeMove);
            if(!isInBounds(getRelativeX(), getRelativeY())) {
                blockEntity.closeApp();
                return;
            }
        }
    }

    public static boolean isInBounds(int relativeX, int relativeY) {
        return relativeX >= 0 && relativeX <= 120 && relativeY >= 0 && relativeY <= 72;
    }

    public void move(SnakeMove snakeMove) {
        this.x += snakeMove.x;
        //this.width += snakeMove.x;
        this.y += snakeMove.y;
        //this.height += snakeMove.y;
    }

    public int getRelativeX() {
        return x - 2;
    }

    public int getRelativeY() {
        return y - 18;
    }

    public void setMovement(SnakeMove snakeMove) {
        this.snakeMove = snakeMove;
    }

    public enum SnakeMove {
        UP(0, 4),
        DOWN(0, -4),
        LEFT(-4, 0),
        RIGHT(4, 0);


        final int x;
        final int y;

        SnakeMove(int x, int y) {
            this.x = x;
            this.y = y;
        }

    }
}
