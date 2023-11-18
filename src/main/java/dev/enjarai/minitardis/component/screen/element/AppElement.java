package dev.enjarai.minitardis.component.screen.element;

import dev.enjarai.minitardis.component.TardisControl;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ClickType;

public interface AppElement {
    void draw(TardisControl controls, DrawableCanvas canvas);

    boolean onClick(TardisControl controls, ServerPlayerEntity player, ClickType type, int x, int y);
}
