package dev.enjarai.minitardis.component.screen.element;

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

    public PlacedElement(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public void draw(TardisControl controls, DrawableCanvas canvas) {
        drawElement(controls, new SubView(canvas, x, y, width, height));
    }

    protected abstract void drawElement(TardisControl controls, DrawableCanvas canvas);

    @Override
    public boolean onClick(TardisControl controls, ServerPlayerEntity player, ClickType type, int x, int y) {
        if (x >= this.x && x < this.x + width && y >= this.y && y < this.y + height) {
            return onClickElement(controls, player, type, x - this.x, y - this.y);
        }
        return false;
    }

    protected abstract boolean onClickElement(TardisControl controls, ServerPlayerEntity player, ClickType type, int x, int y);
}
