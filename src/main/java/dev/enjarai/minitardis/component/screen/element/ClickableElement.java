package dev.enjarai.minitardis.component.screen.element;

import dev.enjarai.minitardis.block.console.ConsoleScreenBlockEntity;
import dev.enjarai.minitardis.component.TardisControl;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ClickType;

import java.util.function.Consumer;

public abstract class ClickableElement extends PlacedElement {
    protected Consumer<TardisControl> clickCallback;
    protected int pressedFrames;

    public ClickableElement(int x, int y, int width, int height, Consumer<TardisControl> clickCallback) {
        super(x, y, width, height);
        this.clickCallback = clickCallback;
    }

    @Override
    protected boolean onClickElement(TardisControl controls, ConsoleScreenBlockEntity blockEntity, ServerPlayerEntity player, ClickType type, int x, int y) {
        if (type == ClickType.RIGHT) {
            pressedFrames = 2;
            clickCallback.accept(controls);
            blockEntity.playClickSound(1);
            return true;
        }
        return false;
    }
}
