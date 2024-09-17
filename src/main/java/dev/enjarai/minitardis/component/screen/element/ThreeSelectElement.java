package dev.enjarai.minitardis.component.screen.element;

import dev.enjarai.minitardis.block.console.ScreenBlockEntity;
import dev.enjarai.minitardis.canvas.TardisCanvasUtils;
import dev.enjarai.minitardis.component.TardisControl;
import dev.enjarai.minitardis.component.screen.canvas.patbox.DrawableCanvas;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ClickType;

import java.util.function.BiConsumer;

public class ThreeSelectElement extends ClickableElement {
    private final BiConsumer<TardisControl, Integer> onUpdate;
    public int state;

    public ThreeSelectElement(int x, int y, int state, BiConsumer<TardisControl, Integer> onUpdate) {
        super(x, y, 14, 14, controls -> {});
        this.state = state;
        this.onUpdate = onUpdate;
    }

    @Override
    protected boolean onClickElement(TardisControl controls, ScreenBlockEntity blockEntity, ServerPlayerEntity player, ClickType type, int x, int y) {
        if (super.onClickElement(controls, blockEntity, player, type, x, y)) {
            state = (state + 1) % 3;
            onUpdate.accept(controls, state);
            return true;
        }
        return false;
    }

    @Override
    protected void drawElement(TardisControl controls, ScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        canvas.draw(0, 0, TardisCanvasUtils.getSprite(pressedFrames > 0 ? "three_select_" + state + "_pressed" : "three_select_" + state));

        if (pressedFrames > 0) pressedFrames--;
    }
}
