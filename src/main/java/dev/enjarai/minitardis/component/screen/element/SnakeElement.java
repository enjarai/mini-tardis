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

    public SnakeElement() {
        super(1, 9, 5, 13);
    }

    @Override
    protected void drawElement(TardisControl controls, ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        CanvasUtils.draw(canvas, x, y, ModCanvasUtils.SNAKE);
    }

    @Override
    protected boolean onClickElement(TardisControl controls, ConsoleScreenBlockEntity blockEntity, ServerPlayerEntity player, ClickType type, int x, int y) {
        return false;
    }

    @Override
    public void tick(TardisControl controls, ConsoleScreenBlockEntity blockEntity) {
        tickCount++;
        if (tickCount % 4 == 0) {
            move(0, 1);
            if(!isInBounds(getRelativeX(), getRelativeY())) {
                blockEntity.closeApp();
                return;
            }
        }
    }

    public boolean isInBounds(int relativeX, int relativeY) {
        return relativeX >= 0 && relativeX <= 60 && relativeY >= 0 && relativeY <= 36;
    }

    public void move(int x, int y) {
        this.x += x;
        this.width += x;
        this.y += y;
        this.height += y;
    }

    public int getRelativeX() {
        return x - 1;
    }

    public int getRelativeY() {
        return y - 9;
    }
}
