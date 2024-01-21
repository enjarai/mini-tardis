package dev.enjarai.minitardis.component.screen.element;

import dev.enjarai.minitardis.block.console.ScreenBlockEntity;
import dev.enjarai.minitardis.canvas.TardisCanvasUtils;
import dev.enjarai.minitardis.component.TardisControl;
import dev.enjarai.minitardis.component.screen.app.SnakeApp;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import eu.pb4.mapcanvas.impl.view.SubView;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ClickType;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import static java.lang.Math.max;
import static java.lang.Math.round;

public class SnakeElement extends PlacedElement {
    private final SnakeApp.SnakeAppView snakeAppView;
    int tickCount;
    public int tailLength;
    public SnakeMove snakeMove = SnakeMove.RIGHT;
    @Nullable
    public SnakeTailElement snakeTail = null;

    public SnakeElement(SnakeApp.SnakeAppView snakeAppView) {
        super(2, 18, 4, 4);
        this.snakeAppView = snakeAppView;
    }

    @Override
    public void draw(TardisControl controls, ScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        super.draw(controls, blockEntity, canvas);
        if(snakeTail != null) {
            this.snakeTail.drawAndPush(
                    controls,
                    blockEntity,
                    new SubView(canvas, snakeTail.x, snakeTail.y, snakeTail.width, snakeTail.height),
                    canvas
            );
        }
    }

    @Override
    protected void drawElement(TardisControl controls, ScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        CanvasUtils.draw(canvas, 0, 0, TardisCanvasUtils.getSprite("snake"));
    }

    @Override
    protected boolean onClickElement(TardisControl controls, ScreenBlockEntity blockEntity, ServerPlayerEntity player, ClickType type, int x, int y) {
        return false;
    }

    @Override
    public void tick(TardisControl controls, ScreenBlockEntity blockEntity) {
        tickCount++;
        if (tickCount % getGameSpeed() == 0) {
            this.snakeAppView.deterministicRandom.skip(this.snakeMove.ordinal());
            this.move(this.snakeMove);
            if(snakeTail != null && snakeTail.doesCollide(this.x, this.y)) {
                this.killedByTail(blockEntity);
                return;
            }
            if(!isInBounds(getRelativeX(), getRelativeY())) {
                this.killedByWall(blockEntity);
                return;
            }
        }
    }

    public boolean doesCollide(int x, int y) {
        return x == this.x && y == this.y || (snakeTail != null && snakeTail.doesCollide(x, y));
    }

    public int getGameSpeed() {
        return max(round(5f - this.tailLength/10f), 2);
    }


    public static boolean isInBounds(int relativeX, int relativeY) {
        return relativeX >= 0 && relativeX <= 120 && relativeY >= 0 && relativeY <= 72;
    }

    public void move(SnakeMove snakeMove) {
        if(snakeTail != null) {
            this.snakeTail.moveToAndPush(this.x, this.y, 1);
        }
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

    public void ateApple() {
        if(this.snakeTail == null) {
            this.snakeTail = new SnakeTailElement(this);
        }
        this.tailLength++;
    }

    public void killedByWall(ScreenBlockEntity blockEntity) {
        this.snakeAppView.snakeDied(blockEntity);
    }

    public void killedByTail(ScreenBlockEntity blockEntity) {
        this.snakeAppView.snakeDied(blockEntity);
    }

    public enum SnakeMove {
        UP(0, 4, MoveType.HORIZONTAL),
        DOWN(0, -4, MoveType.HORIZONTAL),
        LEFT(-4, 0, MoveType.VERTICAL),
        RIGHT(4, 0, MoveType.VERTICAL);


        final int x;
        final int y;
        final MoveType moveType;

        SnakeMove(int x, int y, MoveType moveType) {
            this.x = x;
            this.y = y;
            this.moveType = moveType;
        }

        public SnakeMove getSnakeMove(Vector2i vector2i) {
            //Get Larger vector component and keep data if it is x or y
            return switch (this.moveType) {
                case HORIZONTAL -> vector2i.x > 0 ? RIGHT : LEFT;
                case VERTICAL -> vector2i.y > 0 ? UP : DOWN;
            };
        }

        public enum MoveType {
            HORIZONTAL,
            VERTICAL
        }
    }
}
