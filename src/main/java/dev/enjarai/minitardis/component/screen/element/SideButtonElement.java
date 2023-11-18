package dev.enjarai.minitardis.component.screen.element;

import dev.enjarai.minitardis.canvas.ModCanvasUtils;
import dev.enjarai.minitardis.component.TardisControl;
import eu.pb4.mapcanvas.api.core.CanvasColor;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.api.font.DefaultFonts;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ClickType;

import java.util.function.Consumer;

public class SideButtonElement extends PlacedElement {
    private String text;
    private Consumer<TardisControl> clickCallback;
    private int pressedFrames;

    public SideButtonElement(int x, int y, String text, Consumer<TardisControl> clickCallback) {
        super(x, y, 28, 14);
        this.text = text;
        this.clickCallback = clickCallback;
    }

    @Override
    protected void drawElement(TardisControl controls, DrawableCanvas canvas) {
        CanvasUtils.draw(canvas, 0, 0, pressedFrames > 0 ? ModCanvasUtils.SCREEN_SIDE_BUTTON_PRESSED : ModCanvasUtils.SCREEN_SIDE_BUTTON);
        DefaultFonts.VANILLA.drawText(canvas, text, 2, 4, 8, CanvasColor.WHITE_HIGH);

        if (pressedFrames > 0) pressedFrames--;
    }

    @Override
    protected boolean onClickElement(TardisControl controls, ServerPlayerEntity player, ClickType type, int x, int y) {
        if (type == ClickType.RIGHT) {
            pressedFrames = 2;
            clickCallback.accept(controls);
            return true;
        }
        return false;
    }
}
