package dev.enjarai.minitardis.component.screen.element;

import dev.enjarai.minitardis.block.console.ScreenBlockEntity;
import dev.enjarai.minitardis.canvas.TardisCanvasUtils;
import dev.enjarai.minitardis.component.TardisControl;
import dev.enjarai.minitardis.component.screen.canvas.patbox.DrawableCanvas;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ClickType;

public class ScrollableContainerElement<T extends PlacedElement> extends AbstractParentElement<T> {
    public int scrolledness;

    public ScrollableContainerElement(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    protected void drawElement(TardisControl controls, ScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        var scrolledCanvas = new ScrolledView(canvas, getScrollableHeight());
        for (var element : elements) {
            element.draw(controls, blockEntity, scrolledCanvas);
        }
        canvas.draw(width - 8, 0, TardisCanvasUtils.getSprite("scroll_button_up"));
        canvas.draw(width - 8, height - 38, TardisCanvasUtils.getSprite("scroll_button_down"));
    }

    @Override
    protected boolean onClickElement(TardisControl controls, ScreenBlockEntity blockEntity, ServerPlayerEntity player, ClickType type, int x, int y) {
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

    private class ScrolledView implements DrawableCanvas {
        private final DrawableCanvas source;
        private final int scrollableHeight;

        private ScrolledView(DrawableCanvas source, int scrollableHeight1) {
            this.source = source;
            this.scrollableHeight = scrollableHeight1;
        }

        @Override
        public short getRaw(int x, int y) {
            return source.getRaw(x, y);
        }

        @Override
        public void setRaw(int x, int y, short color) {
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
