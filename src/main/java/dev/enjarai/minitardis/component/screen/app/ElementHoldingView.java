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
    protected final ConsoleScreenBlockEntity blockEntity;

    protected final List<AppElement> children = new ArrayList<>();

    public ElementHoldingView(TardisControl controls, ConsoleScreenBlockEntity blockEntity) {
        this.controls = controls;
        this.blockEntity = blockEntity;
    }

    public <T extends AppElement> T addElement(T element) {
        children.add(element);
        return element;
    }

    public Iterable<AppElement> children(TardisControl controls) {
        return children;
    }

    @Override
    public void draw(DrawableCanvas canvas) {
        children(controls).forEach(el -> el.draw(controls, blockEntity, canvas));
    }

    @Override
    public boolean onClick(ServerPlayerEntity player, ClickType type, int x, int y) {
        for (var element : children(controls)) {
            if (element.onClick(controls, blockEntity, player, type, x, y)) {
                return true;
            }
        }
        return false;
    }
}
