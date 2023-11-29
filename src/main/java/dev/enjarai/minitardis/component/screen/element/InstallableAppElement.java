package dev.enjarai.minitardis.component.screen.element;

import dev.enjarai.minitardis.block.console.ConsoleScreenBlockEntity;
import dev.enjarai.minitardis.canvas.ModCanvasUtils;
import dev.enjarai.minitardis.component.TardisControl;
import dev.enjarai.minitardis.component.screen.app.ScreenApp;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import eu.pb4.mapcanvas.impl.view.SubView;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ClickType;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class InstallableAppElement extends PlacedElement {
    public final ScreenApp app;
    public final boolean installed;
    private final AppSelectorElement parent;
    private final BiFunction<ConsoleScreenBlockEntity, InstallableAppElement, Boolean> moveExecutor;

    public InstallableAppElement(int x, int y, ScreenApp app, boolean installed, AppSelectorElement parent, BiFunction<ConsoleScreenBlockEntity, InstallableAppElement, Boolean> moveExecutor) {
        super(x, y, 26, 26);
        this.app = app;
        this.installed = installed;
        this.parent = parent;
        this.moveExecutor = moveExecutor;
    }

    @Override
    protected void drawElement(TardisControl controls, ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        app.drawIcon(controls, blockEntity, new SubView(canvas, 1, 1, 24, 24));
        if (isSelected()) {
            CanvasUtils.draw(canvas, 0, 0, ModCanvasUtils.APP_SELECTED);
        }
    }

    @Override
    protected boolean onClickElement(TardisControl controls, ConsoleScreenBlockEntity blockEntity, ServerPlayerEntity player, ClickType type, int x, int y) {
        if (!isSelected()) {
            parent.selected = this;
            blockEntity.playClickSound(1.6f);
        } else {
            if (moveExecutor.apply(blockEntity, this)) {
                blockEntity.playClickSound(1.8f);
            } else {
                blockEntity.playClickSound(1.4f);
            }
        }
        return true;
    }

    public boolean isSelected() {
        return parent.selected == this;
    }
}
