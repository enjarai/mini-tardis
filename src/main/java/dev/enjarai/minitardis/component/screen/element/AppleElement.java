package dev.enjarai.minitardis.component.screen.element;

import dev.enjarai.minitardis.block.console.ConsoleScreenBlockEntity;
import dev.enjarai.minitardis.canvas.ModCanvasUtils;
import dev.enjarai.minitardis.component.TardisControl;
import dev.enjarai.minitardis.component.screen.app.SnakeApp;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ClickType;

public class AppleElement extends PlacedElement {
    private final SnakeApp.SnakeAppView snakeAppView;

    public AppleElement(SnakeApp.SnakeAppView snakeAppView) {
        super(6, 22, 4, 4);
        this.snakeAppView = snakeAppView;
    }

    @Override
    protected void drawElement(TardisControl controls, ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        CanvasUtils.draw(canvas, 0, 0, ModCanvasUtils.APPLE);
    }

    @Override
    protected boolean onClickElement(TardisControl controls, ConsoleScreenBlockEntity blockEntity, ServerPlayerEntity player, ClickType type, int x, int y) {
        return false;
    }

    @Override
    public void tick(TardisControl controls, ConsoleScreenBlockEntity blockEntity) {
        if(this.snakeAppView.snake.getRelativeX() == this.getRelativeX() && this.snakeAppView.snake.getRelativeY() == this.getRelativeY()) {
            this.snakeAppView.snake.ateApple();
            this.move(0, 4);
            if(!isInBounds(getRelativeX(), getRelativeY())) {
                this.x = 2;
                this.y = 18;
            }
        }
    }

    public static boolean isInBounds(int relativeX, int relativeY) {
        return relativeX >= 0 && relativeX <= 120 && relativeY >= 0 && relativeY <= 72;
    }

    public void move(int x, int y) {
        this.x += x;
        //this.width += snakeMove.x;
        this.y += y;
        //this.height += snakeMove.y;
    }

    public int getRelativeX() {
        return x - 2;
    }

    public int getRelativeY() {
        return y - 18;
    }
}
