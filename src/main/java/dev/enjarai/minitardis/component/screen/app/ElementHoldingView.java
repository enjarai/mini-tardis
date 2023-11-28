package dev.enjarai.minitardis.component.screen.app;

import dev.enjarai.minitardis.block.console.ConsoleScreenBlockEntity;
import dev.enjarai.minitardis.component.TardisControl;
import dev.enjarai.minitardis.component.screen.element.AppElement;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ClickType;

import java.util.ArrayList;
import java.util.List;

public class ElementHoldingView implements AppView {
    protected final TardisControl controls;

    protected final List<AppElement> children = new ArrayList<>();

    public ElementHoldingView(TardisControl controls) {
        this.controls = controls;
    }

    public <T extends AppElement> T addElement(T element) {
        children.add(element);
        return element;
    }

    public Iterable<AppElement> children() {
        return children;
    }

    @Override
    public void draw(ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        children().forEach(el -> el.draw(controls, blockEntity, canvas));
    }

    @Override
    public boolean onClick(ConsoleScreenBlockEntity blockEntity, ServerPlayerEntity player, ClickType type, int x, int y) {
        for (var element : children()) {
            if (element.onClick(controls, blockEntity, player, type, x, y)) {
                return true;
            }
        }
        return false;
    }
}
