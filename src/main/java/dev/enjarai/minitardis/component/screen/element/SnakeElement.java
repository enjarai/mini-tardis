package dev.enjarai.minitardis.component.screen.element;

import dev.enjarai.minitardis.block.console.ConsoleScreenBlockEntity;
import dev.enjarai.minitardis.canvas.ModCanvasUtils;
import dev.enjarai.minitardis.component.TardisControl;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ClickType;

public class SnakeElement extends PlacedElement {
    public SnakeElement(int x, int y, int width, int height) {
        super(x, y, 11, 11);
    }

    @Override
    protected void drawElement(TardisControl controls, ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        CanvasUtils.draw(canvas, 0, 0, ModCanvasUtils.SNAKE_APP);
    }

    @Override
    protected boolean onClickElement(TardisControl controls, ConsoleScreenBlockEntity blockEntity, ServerPlayerEntity player, ClickType type, int x, int y) {
        return false;
    }
}
