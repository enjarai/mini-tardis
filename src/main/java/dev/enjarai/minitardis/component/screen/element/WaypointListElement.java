package dev.enjarai.minitardis.component.screen.element;

import dev.enjarai.minitardis.component.TardisLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class WaypointListElement extends AbstractParentElement<WaypointElement> {
    @Nullable
    public WaypointElement selected;
    public final Map<Integer, TardisLocation> backingMap;

    public WaypointListElement(int x, int y, int width, int height, Map<Integer, TardisLocation> backingMap) {
        super(x, y, width, height);
        this.backingMap = backingMap;

        for (int i = 0, xi = 0; xi < width / 8; xi++) {
            for (int yi = 0; yi < height / 8; yi++, i++) {
                addElement(new WaypointElement(this, xi * 8, yi * 8, i));
            }
        }
    }
}
