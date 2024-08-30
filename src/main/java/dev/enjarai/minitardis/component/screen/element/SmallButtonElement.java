package dev.enjarai.minitardis.component.screen.element;

import dev.enjarai.minitardis.block.console.ScreenBlockEntity;
import dev.enjarai.minitardis.canvas.TardisCanvasUtils;
import dev.enjarai.minitardis.component.TardisControl;
import dev.enjarai.minitardis.component.screen.canvas.CanvasColors;
import dev.enjarai.minitardis.component.screen.canvas.patbox.DrawableCanvas;
import dev.enjarai.minitardis.component.screen.canvas.patbox.font.DefaultFonts;

import java.util.function.Consumer;

public class SmallButtonElement extends ClickableElement {
    private String text;

    public SmallButtonElement(int x, int y, String text, Consumer<TardisControl> clickCallback) {
        super(x, y, 28, 14, clickCallback);
        this.text = text;
    }

    @Override
    protected void drawElement(TardisControl controls, ScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        canvas.draw(0, 0, pressedFrames > 0 ? TardisCanvasUtils.getSprite("screen_side_button_pressed") : TardisCanvasUtils.getSprite("screen_side_button"));
        DefaultFonts.VANILLA.drawText(canvas, text, 2, 4, 8, CanvasColors.WHITE);

        if (pressedFrames > 0) pressedFrames--;
    }
}
