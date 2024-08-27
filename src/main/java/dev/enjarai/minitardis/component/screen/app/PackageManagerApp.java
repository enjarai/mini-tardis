package dev.enjarai.minitardis.component.screen.app;

import com.mojang.serialization.Codec;
import dev.enjarai.minitardis.block.console.ScreenBlockEntity;
import dev.enjarai.minitardis.canvas.TardisCanvasUtils;
import dev.enjarai.minitardis.component.TardisControl;
import dev.enjarai.minitardis.component.screen.element.AppSelectorElement;
import dev.enjarai.minitardis.component.screen.element.InstallableAppElement;
import dev.enjarai.minitardis.item.FloppyItem;
import eu.pb4.mapcanvas.api.core.CanvasColor;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.api.font.DefaultFonts;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import net.minecraft.item.ItemStack;

public class PackageManagerApp implements ScreenApp {
    public static final Codec<PackageManagerApp> CODEC = Codec.unit(PackageManagerApp::new);

    @Override
    public AppView getView(TardisControl controls) {
        return new ElementHoldingView(controls) {
            private final AppSelectorElement leftElement;
            private final AppSelectorElement rightElement;
            private boolean floppyInserted;

            {
                leftElement = new AppSelectorElement(2, 18);
                rightElement = new AppSelectorElement(65, 18);
                addElement(leftElement);
                addElement(rightElement);
            }

            @Override
            public void draw(ScreenBlockEntity blockEntity, DrawableCanvas canvas) {
                var selected = leftElement.selected == null ? rightElement.selected : leftElement.selected;
                if (selected != null) {
                    DefaultFonts.VANILLA.drawText(canvas, selected.app.getName().getString(), 4, 6, 8, CanvasColor.WHITE_HIGH);
                }

                if (!floppyInserted) {
                    TardisCanvasUtils.drawCenteredText(canvas, "Insert", 2 + 26, 30, CanvasColor.WHITE_HIGH);
                    TardisCanvasUtils.drawCenteredText(canvas, "Floppy", 2 + 26, 40, CanvasColor.WHITE_HIGH);
                } else if (leftElement.getElements().isEmpty()) {
                    TardisCanvasUtils.drawCenteredText(canvas, "Empty", 2 + 26, 30, CanvasColor.LIGHT_GRAY_HIGH);
                }

                super.draw(blockEntity, canvas);
            }

            @Override
            public void screenOpen(ScreenBlockEntity blockEntity) {
                updateInstalledApps();
            }

            @Override
            public void screenTick(ScreenBlockEntity blockEntity) {
                var floppyStack = blockEntity.inventory.getStack(0);
                var newFloppyState = !floppyStack.isEmpty();
                if (floppyInserted != newFloppyState) {
                    updateSourceApps(floppyStack);
                    floppyInserted = newFloppyState;
                }
            }

            private void updateSourceApps(ItemStack floppyStack) {
                leftElement.clearElements();

                var apps = FloppyItem.getApps(floppyStack);
                for (int i = 0; i < apps.size(); i++) {
                    var app = apps.get(i);

                    leftElement.addElement(new InstallableAppElement(
                            i % 2 * 26, i / 2 * 26, app, false,
                            leftElement, el -> rightElement.selected = null, this::installApp
                    ));
                }
            }

            private void updateInstalledApps() {
                rightElement.clearElements();

                var apps = controls.getAllApps().stream().filter(ScreenApp::canBeUninstalled).toList();
                for (int i = 0; i < apps.size(); i++) {
                    var app = apps.get(i);

                    rightElement.addElement(new InstallableAppElement(
                            i % 2 * 26, i / 2 * 26, app, true,
                            rightElement, el -> leftElement.selected = null, this::uninstallApp
                    ));
                }
            }

            private boolean installApp(ScreenBlockEntity blockEntity, InstallableAppElement element) {
                var i = leftElement.getElements().indexOf(element);
                var floppyStack = blockEntity.inventory.getStack(0);

                if (controls.canInstallApp(element.app) && FloppyItem.removeApp(floppyStack, i)) {
                    controls.installApp(element.app);

                    updateInstalledApps();
                    updateSourceApps(floppyStack);

                    return true;
                }

                return false;
            }

            private boolean uninstallApp(ScreenBlockEntity blockEntity, InstallableAppElement element) {
                if (floppyInserted) {
                    var floppyStack = blockEntity.inventory.getStack(0);

                    if (controls.canUninstallApp(element.app.getType())) {
                        FloppyItem.addApp(floppyStack, element.app);
                        controls.uninstallApp(element.app.getType());

                        updateInstalledApps();
                        updateSourceApps(floppyStack);

                        return true;
                    }
                }
                return false;
            }

            @Override
            public void drawBackground(ScreenBlockEntity blockEntity, DrawableCanvas canvas) {
                CanvasUtils.draw(canvas, 0, 0, TardisCanvasUtils.getSprite("package_manager_background"));
            }
        };
    }

    @Override
    public void drawIcon(TardisControl controls, ScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        CanvasUtils.draw(canvas, 0, 0, TardisCanvasUtils.getSprite("app/package_manager"));
    }

    @Override
    public boolean canBeUninstalled() {
        return false;
    }

    @Override
    public ScreenAppType<?> getType() {
        return ScreenAppTypes.PACKAGE_MANAGER;
    }
}
