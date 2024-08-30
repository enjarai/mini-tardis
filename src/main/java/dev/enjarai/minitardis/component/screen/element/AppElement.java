package dev.enjarai.minitardis.component.screen.element;

import dev.enjarai.minitardis.block.console.ScreenBlockEntity;
import dev.enjarai.minitardis.component.TardisControl;
import dev.enjarai.minitardis.component.screen.canvas.patbox.DrawableCanvas;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ClickType;

public interface AppElement {
    void draw(TardisControl controls, ScreenBlockEntity blockEntity, DrawableCanvas canvas);

    void tick(TardisControl controls, ScreenBlockEntity blockEntity);

    boolean onClick(TardisControl controls, ScreenBlockEntity blockEntity, ServerPlayerEntity player, ClickType type, int x, int y);
}
