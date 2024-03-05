package dev.enjarai.minitardis.component.screen.element;

import dev.enjarai.minitardis.block.console.ScreenBlockEntity;
import dev.enjarai.minitardis.component.TardisControl;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.impl.view.SubView;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ClickType;

public abstract class PlacedElement implements AppElement {
    public int x;
    public int y;
    public int width;
    public int height;
    public boolean visible = true;

    public PlacedElement(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public boolean overlapsWith(PlacedElement other) {
        return Math.max(x, other.x) <= Math.min(x + width, other.x + other.width) &&
                Math.max(y, other.y) <= Math.min(y + height, other.y + other.height);
    }

    @Override
    public void draw(TardisControl controls, ScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        if (visible) {
            drawElement(controls, blockEntity, new SubView(canvas, x, y, width, height));
        }
    }

    @Override
    public void tick(TardisControl controls, ScreenBlockEntity blockEntity) {

    }

    protected abstract void drawElement(TardisControl controls, ScreenBlockEntity blockEntity, DrawableCanvas canvas);

    @Override
    public boolean onClick(TardisControl controls, ScreenBlockEntity blockEntity, ServerPlayerEntity player, ClickType type, int x, int y) {
        if (visible && x >= this.x && x < this.x + width && y >= this.y && y < this.y + height) {
            return onClickElement(controls, blockEntity, player, type, x - this.x, y - this.y);
        }
        return false;
    }

    protected abstract boolean onClickElement(TardisControl controls, ScreenBlockEntity blockEntity, ServerPlayerEntity player, ClickType type, int x, int y);
}
