package dev.enjarai.minitardis.component.screen.element;

import dev.enjarai.minitardis.block.console.ConsoleScreenBlockEntity;
import dev.enjarai.minitardis.canvas.ModCanvasUtils;
import dev.enjarai.minitardis.component.TardisControl;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ClickType;

import java.util.ArrayList;
import java.util.List;

public class ScrollableContainerElement<T extends PlacedElement> extends AbstractParentElement<T> {
    public int scrolledness;

    public ScrollableContainerElement(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    protected void drawElement(TardisControl controls, ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        var scrolledCanvas = new ScrolledView(canvas, getScrollableHeight());
        for (var element : elements) {
            element.draw(controls, blockEntity, scrolledCanvas);
        }
        CanvasUtils.draw(canvas, width - 8, 0, ModCanvasUtils.SCROLL_BUTTON_UP);
        CanvasUtils.draw(canvas, width - 8, height - 38, ModCanvasUtils.SCROLL_BUTTON_DOWN);
    }

    @Override
    protected boolean onClickElement(TardisControl controls, ConsoleScreenBlockEntity blockEntity, ServerPlayerEntity player, ClickType type, int x, int y) {
        if (x >= width - 8) {
            if (y < 38) {
                scrolledness = Math.max(0, scrolledness - 16);
            } else if (y >= height - 38) {
                scrolledness = Math.min(getScrollableHeight() - height, scrolledness + 16);
            }
            blockEntity.playClickSound(1.8f);
            return true;
        }

        for (var element : elements) {
            if (element.onClick(controls, blockEntity, player, type, x, y + scrolledness)) {
                return true;
            }
        }
        return false;
    }

    public int getScrollableHeight() {
        return elements.stream()
                .map(el -> el.y + el.height)
                .max(Integer::compareTo)
                .orElse(0);
    }

    @SuppressWarnings("NonExtendableApiUsage")
    private class ScrolledView implements DrawableCanvas {
        private final DrawableCanvas source;
        private final int scrollableHeight;

        private ScrolledView(DrawableCanvas source, int scrollableHeight1) {
            this.source = source;
            this.scrollableHeight = scrollableHeight1;
        }

        @Override
        public byte getRaw(int x, int y) {
            return source.getRaw(x, y);
        }

        @Override
        public void setRaw(int x, int y, byte color) {
            y -= scrolledness;
            if (y >= 0 && y < height) {
                source.setRaw(x, y, color);
            }
        }

        @Override
        public int getHeight() {
            return scrollableHeight;
        }

        @Override
        public int getWidth() {
            return width;
        }
    }
}
