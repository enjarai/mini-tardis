package dev.enjarai.minitardis.component.screen.app;

import com.mojang.serialization.Codec;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.block.console.ConsoleScreenBlockEntity;
import dev.enjarai.minitardis.canvas.ModCanvasUtils;
import dev.enjarai.minitardis.component.TardisControl;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import net.minecraft.util.Identifier;

public class PackageManagerApp extends ElementHoldingApp {
    public static final Codec<PackageManagerApp> CODEC = Codec.unit(PackageManagerApp::new);
    public static final Identifier ID = MiniTardis.id("package_manager");

    @Override
    public void draw(TardisControl controls, ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        super.draw(controls, blockEntity, canvas);
    }

    @Override
    public void drawIcon(TardisControl controls, ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        CanvasUtils.draw(canvas, 0, 0, ModCanvasUtils.PACKAGE_MANAGER_APP);
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
