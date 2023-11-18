package dev.enjarai.minitardis.component.screen;

import com.mojang.serialization.Codec;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.canvas.ModCanvasUtils;
import dev.enjarai.minitardis.component.DestinationScanner;
import dev.enjarai.minitardis.component.TardisControl;
import dev.enjarai.minitardis.component.screen.element.SideButtonElement;
import eu.pb4.mapcanvas.api.core.CanvasColor;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import eu.pb4.mapcanvas.impl.view.RotatedView;
import net.minecraft.util.Identifier;

public class ScannerApp extends ElementHoldingApp {
    public static final Codec<ScannerApp> CODEC = Codec.unit(ScannerApp::new);
    public static final Identifier ID = MiniTardis.id("scanner");

    public ScannerApp() {
        addElement(new SideButtonElement(96 + 2, 2 + 14, "XAxis", controls -> controls.getTardis().getDestinationScanner().useXAxis()));
        addElement(new SideButtonElement(96 + 2, 2 + 14 + 14, "ZAxis", controls -> controls.getTardis().getDestinationScanner().useZAxis()));
    }

    @Override
    public void draw(TardisControl controls, DrawableCanvas canvas) {
        for (int x = 0; x < DestinationScanner.RANGE; x++) {
            for (int y = 0; y < DestinationScanner.RANGE; y++) {
                byte value = controls.getTardis().getDestinationScanner().getFor(x, y);
                canvas.set(
                        x, -y - 1 + DestinationScanner.RANGE,
                        switch (value) {
                            case 0 -> CanvasColor.BLACK_HIGH;
                            case 1 -> CanvasColor.DEEPSLATE_GRAY_HIGH;
                            case 2 -> CanvasColor.BLUE_NORMAL;
                            case 3 -> CanvasColor.LIGHT_BLUE_NORMAL;
                            case 4 -> CanvasColor.ORANGE_LOWEST;
                            default -> CanvasColor.WHITE_HIGH;
                        });
            }
        }
        canvas.set(DestinationScanner.RANGE / 2 - 1, DestinationScanner.RANGE / 2, CanvasColor.ORANGE_HIGH);
        canvas.set(DestinationScanner.RANGE / 2 - 1, DestinationScanner.RANGE / 2 - 1, CanvasColor.ORANGE_HIGH);

        CanvasUtils.draw(canvas, 96, 64, ModCanvasUtils.COORD_WIDGET);
        controls.getTardis().getDestination().ifPresent(destination ->
                CanvasUtils.draw(canvas, 96, 64, new RotatedView(ModCanvasUtils.FACING_WIDGET, Math.toRadians(destination.facing().asRotation()), 0, 0)));

        super.draw(controls, canvas);
    }

    @Override
    public void drawIcon(TardisControl controls, DrawableCanvas canvas) {
        CanvasUtils.fill(canvas, 0, 0, 24, 24, CanvasColor.YELLOW_HIGH);
    }

    @Override
    public void screenTick(TardisControl controls) {
        controls.getTardis().getDestinationScanner().shouldScanNextTick();
    }

    @Override
    public Identifier id() {
        return ID;
    }
}
