package dev.enjarai.minitardis.component.screen.app;

import com.mojang.serialization.Codec;
import dev.enjarai.minitardis.block.console.ScreenBlockEntity;
import dev.enjarai.minitardis.canvas.TardisCanvasUtils;
import dev.enjarai.minitardis.component.DestinationScanner;
import dev.enjarai.minitardis.component.TardisControl;
import dev.enjarai.minitardis.component.screen.element.SmallButtonElement;
import eu.pb4.mapcanvas.api.core.CanvasColor;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import eu.pb4.mapcanvas.impl.view.Rotate90ClockwiseView;

public class ScannerApp implements ScreenApp {
    public static final Codec<ScannerApp> CODEC = Codec.unit(ScannerApp::new);

    @Override
    public AppView getView(TardisControl controls) {
        return new ElementHoldingView(controls) {
            {
                addElement(new SmallButtonElement(96 + 2, 2 + 14, "XAxis", controls -> controls.getTardis().getDestinationScanner().useXAxis()));
                addElement(new SmallButtonElement(96 + 2, 2 + 14 + 14, "ZAxis", controls -> controls.getTardis().getDestinationScanner().useZAxis()));
            }

            @Override
            public void draw(ScreenBlockEntity blockEntity, DrawableCanvas canvas) {
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
                                    case 4 -> CanvasColor.ORANGE_HIGH;
                                    default -> CanvasColor.WHITE_HIGH;
                                });
                    }
                }
                canvas.set(DestinationScanner.RANGE / 2 - 1, DestinationScanner.RANGE / 2, CanvasColor.ORANGE_NORMAL);
                canvas.set(DestinationScanner.RANGE / 2 - 1, DestinationScanner.RANGE / 2 - 1, CanvasColor.ORANGE_NORMAL);

                CanvasUtils.draw(canvas, 96, 64, controls.getTardis().getDestinationScanner().isZAxis() ? TardisCanvasUtils.getSprite("coord_widget_z") : TardisCanvasUtils.getSprite("coord_widget_x"));
                controls.getTardis().getDestination().ifPresent(destination -> {
                    DrawableCanvas view = TardisCanvasUtils.getSprite("destination_facing_widget");
                    for (int i = 0; i < destination.facing().getHorizontal(); i++) {
                        view = new Rotate90ClockwiseView(view);
                    }
                    CanvasUtils.draw(canvas, 96, 64, view);
                });

                super.draw(blockEntity, canvas);
            }

            @Override
            public void screenTick(ScreenBlockEntity blockEntity) {
                controls.getTardis().getDestinationScanner().shouldScanNextTick();
            }
        };
    }


    @Override
    public void drawIcon(TardisControl controls, ScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        CanvasUtils.draw(canvas, 0, 0, TardisCanvasUtils.getSprite("app/scanner"));
    }

    @Override
    public ScreenAppType<?> getType() {
        return ScreenAppTypes.SCANNER;
    }
}
