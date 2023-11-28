package dev.enjarai.minitardis.component.screen.element;

import dev.enjarai.minitardis.block.console.ConsoleScreenBlockEntity;
import dev.enjarai.minitardis.component.TardisControl;
import dev.enjarai.minitardis.component.screen.app.ScreenApp;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.impl.view.SubView;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ClickType;
import net.minecraft.util.Identifier;

public class InstallableAppElement extends PlacedElement {
    public final Identifier app;
    private final ScreenApp appInstance;
    public boolean installed;

    public InstallableAppElement(int x, int y, Identifier app, boolean installed) {
        super(x, y, 26, 26);
        this.app = app;
        this.installed = installed;
        this.appInstance = ScreenApp.CONSTRUCTORS.get(app).get();
    }

    @Override
    protected void drawElement(TardisControl controls, ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        appInstance.drawIcon(controls, blockEntity, new SubView(canvas, 1, 1, 24, 24));
    }

    @Override
    protected boolean onClickElement(TardisControl controls, ConsoleScreenBlockEntity blockEntity, ServerPlayerEntity player, ClickType type, int x, int y) {
        return false;
    }
}
