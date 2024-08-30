package dev.enjarai.minitardis.component.screen.element;

import dev.enjarai.minitardis.block.console.ScreenBlockEntity;
import dev.enjarai.minitardis.canvas.TardisCanvasUtils;
import dev.enjarai.minitardis.component.TardisControl;
import dev.enjarai.minitardis.component.screen.canvas.patbox.DrawableCanvas;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

public class DimensionStarElement extends ClickableElement {
    public final RegistryKey<World> worldKey;

    public DimensionStarElement(int x, int y, RegistryKey<World> worldKey) {
        super(x, y, 11, 11, controls -> controls.moveDestinationToDimension(worldKey));
        this.worldKey = worldKey;
    }

    @Override
    protected void drawElement(TardisControl controls, ScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        var isSelected = controls.getTardis().getDestination().map(l -> l.worldKey().equals(worldKey)).orElse(false);
        canvas.draw(0, 0, isSelected ? TardisCanvasUtils.getSprite("dimension_marker_selected") : TardisCanvasUtils.getSprite("dimension_marker"));
    }
}
