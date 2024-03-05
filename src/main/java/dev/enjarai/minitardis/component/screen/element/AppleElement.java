package dev.enjarai.minitardis.component.screen.element;

import dev.enjarai.minitardis.block.console.ScreenBlockEntity;
import dev.enjarai.minitardis.canvas.TardisCanvasUtils;
import dev.enjarai.minitardis.component.TardisControl;
import dev.enjarai.minitardis.component.screen.app.SnakeApp;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ClickType;
import org.joml.Vector2i;

import java.util.ArrayList;

public class AppleElement extends PlacedElement {
    private final SnakeApp.SnakeAppView snakeAppView;

    public AppleElement(SnakeApp.SnakeAppView snakeAppView) {
        super(6, 22, 4, 4);
        this.snakeAppView = snakeAppView;
    }

    @Override
    protected void drawElement(TardisControl controls, ScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        CanvasUtils.draw(canvas, 0, 0, TardisCanvasUtils.getSprite("apple"));
    }

    @Override
    protected boolean onClickElement(TardisControl controls, ScreenBlockEntity blockEntity, ServerPlayerEntity player, ClickType type, int x, int y) {
        return false;
    }

    private final ArrayList<Vector2i> freePoses = new ArrayList<>((19 * 30) - 2);

    @Override
    public void tick(TardisControl controls, ScreenBlockEntity blockEntity) {
        if(this.snakeAppView.snake.getRelativeX() == this.getRelativeX() && this.snakeAppView.snake.getRelativeY() == this.getRelativeY()) {
            this.snakeAppView.ateApple(blockEntity);

            for (int x = 0; x < 30; x++) {
                for (int y = 0; y < 19; y++) {
                    if(x == this.x && y == this.y) continue;
                    if(this.snakeAppView.snake.doesCollide(x, y)) continue;
                    freePoses.add(new Vector2i(x, y));
                }
            }
            if(freePoses.isEmpty()) {
                this.snakeAppView.won(blockEntity);
            }
            var pos = freePoses.get(this.snakeAppView.deterministicRandom.nextBetween(0, freePoses.size() - 1));
            this.x = pos.x * 4 + 2;
            this.y = pos.y * 4 + 18;

            if(!isInBounds()) {
                this.x = 6;
                this.y = 22;
            }
        }
    }

    public boolean isInBounds() {
        int relativeX = this.getRelativeX();
        int relativeY = this.getRelativeY();
        return relativeX >= 0 && relativeX <= 122 && relativeY >= 0 && relativeY <= 72;
    }

    public int getRelativeX() {
        return x - 2;
    }

    public int getRelativeY() {
        return y - 18;
    }
}
