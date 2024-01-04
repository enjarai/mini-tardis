package dev.enjarai.minitardis.component.screen.element;

import dev.enjarai.minitardis.block.console.ConsoleScreenBlockEntity;
import dev.enjarai.minitardis.canvas.TardisCanvasUtils;
import dev.enjarai.minitardis.component.TardisControl;
import eu.pb4.mapcanvas.api.core.CanvasColor;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.api.font.DefaultFonts;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import eu.pb4.mapcanvas.impl.view.SubView;

import java.util.function.Consumer;

public class ResizableButtonElement extends ClickableElement {
    private String text;

    public ResizableButtonElement(int x, int y, int width, String text, Consumer<TardisControl> clickCallback) {
        super(x, y, width, 14, clickCallback);
        this.text = text;
    }

    @Override
    protected void drawElement(TardisControl controls, ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        var sprite = pressedFrames > 0 ? TardisCanvasUtils.getSprite("screen_side_button_pressed") : TardisCanvasUtils.getSprite("screen_side_button");
        CanvasUtils.draw(canvas, 0, 0, new SubView(sprite, 0, 0, 2, 14));
        var tileArea = new SubView(sprite, 2, 0, 24, 14);
        for (int i = 0; i < width / 24 + 1; i++) {
            CanvasUtils.draw(canvas, 2 + i * 24, 0, tileArea);
        }
        CanvasUtils.draw(canvas, width - 2, 0, new SubView(sprite, 26, 0, 2, 14));
        TardisCanvasUtils.drawCenteredText(canvas, text, width / 2, 4, CanvasColor.WHITE_HIGH);

        if (pressedFrames > 0) pressedFrames--;
    }
}
