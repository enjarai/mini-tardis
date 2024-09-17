package dev.enjarai.minitardis.component.screen.app;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.enjarai.minitardis.block.console.ScreenBlockEntity;
import dev.enjarai.minitardis.canvas.TardisCanvasUtils;
import dev.enjarai.minitardis.component.FlightWave;
import dev.enjarai.minitardis.component.Tardis;
import dev.enjarai.minitardis.component.TardisControl;
import dev.enjarai.minitardis.component.TardisLocation;
import dev.enjarai.minitardis.component.screen.canvas.patbox.DrawableCanvas;
import dev.enjarai.minitardis.component.screen.element.InterdictionSliderElement;
import dev.enjarai.minitardis.component.screen.element.ResizableButtonElement;
import dev.enjarai.minitardis.component.screen.element.ThreeSelectElement;
import net.minecraft.util.math.Direction;

public class InterdictorApp implements ScreenApp {
    public static final Codec<InterdictorApp> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("selected_wave_part", 0).forGetter(app -> app.selectedWavePart),
            FlightWave.CODEC.optionalFieldOf("selected_wave", new FlightWave(0.5, 0.5, 0.5)).forGetter(app -> app.selectedWave)
    ).apply(instance, InterdictorApp::new));

    private int selectedWavePart;
    private final FlightWave selectedWave;

    private InterdictorApp(int selectedWavePart, FlightWave selectedWave) {
        this.selectedWavePart = selectedWavePart;
        this.selectedWave = selectedWave;
    }

    public InterdictorApp() {
        this(0, new FlightWave(0.5, 0.5, 0.5));
    }

    @Override
    public AppView getView(TardisControl controls) {
        return new ElementHoldingView(controls) {
            private final ThreeSelectElement wavePartElement;
            private final InterdictionSliderElement waveSliderElement;
            private final ResizableButtonElement restartButton;

            {
                waveSliderElement = addElement(new InterdictionSliderElement(
                        16, 2, selectedWave.getValue(selectedWavePart),
                        (controls, state) -> selectedWave.setValue(selectedWavePart, state)
                ));
                wavePartElement = addElement(new ThreeSelectElement(
                        2, 2, selectedWavePart,
                        (controls, state) -> {
                            selectedWavePart = state;
                            waveSliderElement.state = selectedWave.getValue(state);
                        }
                ));
                restartButton = addElement(new ResizableButtonElement(62 - 35, 70, 70, "Lock Target", controls -> {
                    for (Tardis otherTardis : controls.getTardis().getHolder().getInterdictableTardii()) {
                        if (selectedWave.equals(otherTardis.getFlightWave())) {
                            var random = controls.getTardis().getRandom();
                            controls.getTardis().setDestination(
                                    new TardisLocation(
                                            otherTardis.getInteriorWorld().getRegistryKey(),
                                            otherTardis.getInteriorDoorPosition().add(
                                                    random.nextBetween(-10, 10),
                                                    random.nextBetween(-10, 10),
                                                    random.nextBetween(-10, 10)
                                            ),
                                            controls.getTardis().getDestination().map(TardisLocation::facing).orElse(Direction.NORTH)
                                    ),
                                    false
                            );
                        }
                    }
                }));
                restartButton.visible = false;
            }

            @Override
            public void draw(ScreenBlockEntity blockEntity, DrawableCanvas canvas) {
                var tardis = controls.getTardis();
                var timeyTimey = tardis.getInteriorWorld().getTime() * 2;

                if (tardis.getState().canBeInterdicted(tardis)) {
                    drawWave(canvas, tardis.getFlightWave(), timeyTimey, (short) 0xf222);
                }

                var lockable = false;
                for (Tardis otherTardis : tardis.getHolder().getInterdictableTardii()) {
                    drawWave(canvas, otherTardis.getFlightWave(), timeyTimey, (short) 0xf383);
                    if (selectedWave.equals(otherTardis.getFlightWave())) {
                        lockable = true;
                    }
                }
                restartButton.visible = lockable;

                drawWave(canvas, selectedWave, timeyTimey, (short) 0xfc80);

                if (tardis.getDestinationWorld().map(w -> Tardis.isTardis(w.getRegistryKey())).orElse(false)) {
                    canvas.draw(0, 0, TardisCanvasUtils.getSprite("interdictor_target_locked"));
                    restartButton.pressedFrames = 1;
                }

                super.draw(blockEntity, canvas);
            }

            void drawWave(DrawableCanvas canvas, FlightWave wave, double offset, short color) {
                for (int x = 3; x < 126; x++) {
                    int y1 = 55 + (int) (Math.sin((x + offset + wave.getOffset() * 35.0 - 1) * wave.getPeriod() * 0.5) * 35.0 * wave.getMagnitude());
                    int y2 = 55 + (int) (Math.sin((x + offset + wave.getOffset() * 35.0) * wave.getPeriod() * 0.5) * 35.0 * wave.getMagnitude());
                    canvas.bresenhamLine(x - 1, y1, x, y2, color);
                }
            }

            @Override
            public void drawBackground(ScreenBlockEntity blockEntity, DrawableCanvas canvas) {
                canvas.draw(0, 0, TardisCanvasUtils.getSprite("interdictor_background"));
            }
        };
    }

    @Override
    public ScreenAppType<?> getType() {
        return ScreenAppTypes.INTERDICTOR;
    }
}
