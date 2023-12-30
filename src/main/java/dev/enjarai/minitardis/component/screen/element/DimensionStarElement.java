package dev.enjarai.minitardis.component.screen.element;

import dev.enjarai.minitardis.block.console.ConsoleScreenBlockEntity;
import dev.enjarai.minitardis.canvas.ModCanvasUtils;
import dev.enjarai.minitardis.component.TardisControl;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

public class DimensionStarElement extends ClickableElement {
    public final RegistryKey<World> worldKey;

    public DimensionStarElement(int x, int y, RegistryKey<World> worldKey) {
        super(x, y, 11, 11, controls -> controls.moveDestinationToDimension(worldKey));
        this.worldKey = worldKey;
    }

    @Override
    protected void drawElement(TardisControl controls, ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        var isSelected = controls.getTardis().getDestination().map(l -> l.worldKey().equals(worldKey)).orElse(false);
        CanvasUtils.draw(canvas, 0, 0, isSelected ? ModCanvasUtils.DIMENSION_MARKER_SELECTED : ModCanvasUtils.DIMENSION_MARKER);
    }
}
