package dev.enjarai.minitardis.component.screen.app;

import com.mojang.serialization.Codec;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.block.console.ConsoleScreenBlockEntity;
import dev.enjarai.minitardis.canvas.ModCanvasUtils;
import dev.enjarai.minitardis.component.TardisControl;
import dev.enjarai.minitardis.component.screen.element.InstallableAppElement;
import dev.enjarai.minitardis.component.screen.element.ScrollableContainerElement;
import eu.pb4.mapcanvas.api.core.CanvasColor;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import net.minecraft.util.Identifier;

public class PackageManagerApp extends ElementHoldingApp {
    public static final Codec<PackageManagerApp> CODEC = Codec.unit(PackageManagerApp::new);
    public static final Identifier ID = MiniTardis.id("package_manager");

    private final ScrollableContainerElement leftElement;
    private final ScrollableContainerElement rightElement;

    public PackageManagerApp() {
        leftElement = new ScrollableContainerElement(2, 18, 61, 76);
        rightElement = new ScrollableContainerElement(65, 18, 61, 76);
        addElement(leftElement);
        addElement(rightElement);
    }

    @Override
    public void draw(TardisControl controls, ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        if (blockEntity.inventory.getStack(0).isEmpty()) {
            ModCanvasUtils.drawCenteredText(canvas, "Insert", 2 + 26, 30, CanvasColor.WHITE_HIGH);
            ModCanvasUtils.drawCenteredText(canvas, "Floppy", 2 + 26, 40, CanvasColor.WHITE_HIGH);
        } else if (leftElement.elements.isEmpty()) {
            ModCanvasUtils.drawCenteredText(canvas, "Empty", 2 + 26, 30, CanvasColor.LIGHT_GRAY_HIGH);
        }
        super.draw(controls, blockEntity, canvas);
    }

    @Override
    public void screenOpen(TardisControl controls, ConsoleScreenBlockEntity blockEntity) {
        leftElement.elements.clear();
        rightElement.elements.clear();
        leftElement.scrolledness = 0;
        rightElement.scrolledness = 0;

        var apps = controls.getAllApps();
        for (int i = 0; i < apps.size(); i++) {
            var app = apps.get(i);

            if (app.canBeUninstalled()) {
                rightElement.elements.add(new InstallableAppElement(i % 2 * 26, i / 2 * 26, app.id(), true));
            }
        }
    }

    protected void updateSourceApps(ConsoleScreenBlockEntity blockEntity) {

    }

    @Override
    public void drawIcon(TardisControl controls, ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        CanvasUtils.draw(canvas, 0, 0, ModCanvasUtils.PACKAGE_MANAGER_APP);
    }

    @Override
    public void drawBackground(TardisControl control, ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        CanvasUtils.draw(canvas, 0, 0, ModCanvasUtils.PACKAGE_MANAGER_BACKGROUND);
    }

    @Override
    public boolean canBeUninstalled() {
        return false;
    }

    @Override
    public Identifier id() {
        return ID;
    }
}
