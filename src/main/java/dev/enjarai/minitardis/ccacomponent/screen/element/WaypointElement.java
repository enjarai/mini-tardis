package dev.enjarai.minitardis.ccacomponent.screen.element;

import dev.enjarai.minitardis.block.console.ScreenBlockEntity;
import dev.enjarai.minitardis.canvas.TardisCanvasUtils;
import dev.enjarai.minitardis.ccacomponent.TardisControl;
import dev.enjarai.minitardis.ccacomponent.TardisLocation;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ClickType;

public class WaypointElement extends PlacedElement {
    private final WaypointListElement parent;
    private final int index;

    public WaypointElement(WaypointListElement parent, int x, int y, int index) {
        super(x, y, 8, 8);
        this.parent = parent;
        this.index = index;
    }

    @Override
    protected void drawElement(TardisControl controls, ScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        if (parent.backingMap.get(index) == null) {
            CanvasUtils.draw(canvas, 0, 0, isSelected() ? TardisCanvasUtils.getSprite("waypoint_empty_selected") : TardisCanvasUtils.getSprite("waypoint_empty"));
        } else {
            CanvasUtils.draw(canvas, 0, 0, isSelected() ? TardisCanvasUtils.getSprite("waypoint_filled_selected") : TardisCanvasUtils.getSprite("waypoint_filled"));
        }
    }

    @Override
    protected boolean onClickElement(TardisControl controls, ScreenBlockEntity blockEntity, ServerPlayerEntity player, ClickType type, int x, int y) {
        parent.selected = this;
        blockEntity.playClickSound(0.9f);
        return true;
    }

    public boolean isSelected() {
        return parent.selected == this;
    }

    public TardisLocation getWaypointValue() {
        return parent.backingMap.get(index);
    }

    public void setWaypointValue(TardisLocation location) {
        parent.backingMap.put(index, location);
    }

    public void clearWaypointValue() {
        parent.backingMap.remove(index);
    }
}
