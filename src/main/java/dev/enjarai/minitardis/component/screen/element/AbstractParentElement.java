package dev.enjarai.minitardis.component.screen.element;

import dev.enjarai.minitardis.block.console.ConsoleScreenBlockEntity;
import dev.enjarai.minitardis.component.TardisControl;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ClickType;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractParentElement<T extends PlacedElement> extends PlacedElement {
    protected final List<T> elements = new ArrayList<>();

    public AbstractParentElement(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    protected void drawElement(TardisControl controls, ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        for (var element : elements) {
            element.draw(controls, blockEntity, canvas);
        }
    }

    @Override
    protected boolean onClickElement(TardisControl controls, ConsoleScreenBlockEntity blockEntity, ServerPlayerEntity player, ClickType type, int x, int y) {
        for (var element : elements) {
            if (element.onClick(controls, blockEntity, player, type, x, y)) {
                return true;
            }
        }
        return false;
    }

    public void addElement(T element) {
        elements.add(element);
    }

    public void removeElement(T element) {
        elements.remove(element);
    }

    public List<T> getElements() {
        return elements;
    }

    public void clearElements() {
        elements.clear();
    }
}
