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
import net.minecraft.block.MapColor;

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
                        var color = CanvasColor.from(MapColor.get(value), MapColor.Brightness.NORMAL);
                        canvas.set(x, -y - 1 + DestinationScanner.RANGE, color);
                    }
                }
                canvas.set(DestinationScanner.RANGE / 2 - 1, DestinationScanner.RANGE / 2, CanvasColor.ORANGE_NORMAL);
                canvas.set(DestinationScanner.RANGE / 2 - 1, DestinationScanner.RANGE / 2 - 1, CanvasColor.ORANGE_NORMAL);

                var isZ = controls.getTardis().getDestinationScanner().isZAxis();
                CanvasUtils.draw(canvas, 96, 64, isZ ? TardisCanvasUtils.getSprite("coord_widget_z") : TardisCanvasUtils.getSprite("coord_widget_x"));
                controls.getTardis().getDestination().ifPresent(destination -> {
                    var rotation = destination.facing().getHorizontal();
                    if (isZ) {
                        rotation = (rotation + 3) % 4;
                    }

                    DrawableCanvas view = TardisCanvasUtils.getSprite("destination_facing_widget");
                    for (int i = 0; i < rotation; i++) {
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
