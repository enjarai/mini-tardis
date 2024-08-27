package dev.enjarai.minitardis.component.screen.element;

import org.jetbrains.annotations.Nullable;

public class AppSelectorElement extends ScrollableContainerElement<InstallableAppElement> {
    @Nullable
    public InstallableAppElement selected;

    public AppSelectorElement(int x, int y) {
        super(x, y, 61, 76);
    }

    @Override
    public void removeElement(InstallableAppElement element) {
        super.removeElement(element);
        if (element == selected) {
            selected = null;
        }
    }

    @Override
    public void clearElements() {
        super.clearElements();
        selected = null;
    }
}
