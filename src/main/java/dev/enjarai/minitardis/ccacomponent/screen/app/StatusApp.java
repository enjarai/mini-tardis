package dev.enjarai.minitardis.ccacomponent.screen.app;

import com.mojang.serialization.Codec;
import dev.enjarai.minitardis.block.console.ScreenBlockEntity;
import dev.enjarai.minitardis.canvas.TardisCanvasUtils;
import dev.enjarai.minitardis.ccacomponent.TardisControl;
import dev.enjarai.minitardis.ccacomponent.flight.DriftingState;
import dev.enjarai.minitardis.ccacomponent.flight.FlyingState;
import dev.enjarai.minitardis.ccacomponent.flight.RefuelingState;
import eu.pb4.mapcanvas.api.core.CanvasColor;
import eu.pb4.mapcanvas.api.core.CanvasImage;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.api.font.DefaultFonts;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ClickType;
import net.minecraft.util.math.random.LocalRandom;
import net.minecraft.util.math.random.Random;

import java.util.Arrays;

public class StatusApp implements ScreenApp {
    public static final Codec<StatusApp> CODEC = Codec.unit(StatusApp::new);

    private Random etaRandom = new LocalRandom(0);

    @Override
    public AppView getView(TardisControl controls) {
        return new AppView() {
            final int[] lastOffsets = new int[8];

            @Override
            public void draw(ScreenBlockEntity blockEntity, DrawableCanvas canvas) {
                var tardis = controls.getTardis();
                var time = tardis.getInteriorWorld().getTime();

                DefaultFonts.VANILLA.drawText(canvas, tardis.getState().getName().getString(), 4, 6, 8, CanvasColor.WHITE_HIGH);

                tardis.getState(DriftingState.class).ifPresent(state -> {
                    for (int i = 0; i < state.phaseCount; i++) {
                        CanvasImage icon;

                        if (state.phasesComplete > i) {
                            icon = TardisCanvasUtils.getSprite("drifting_phase_complete");
                        } else if (state.phasesComplete >= i && state.phaseTicks >= state.phaseLength) {
                            icon = TardisCanvasUtils.getSprite("drifting_phase_available");
                        } else {
                            icon = TardisCanvasUtils.getSprite("drifting_phase");
                        }

                        CanvasUtils.draw(canvas, 8 + i * 8, 24, icon);
                    }
                });

                tardis.getState(FlyingState.class).ifPresent(state -> {
                    for (int i = 0; i < state.offsets.length; i++) {
                        int offset = state.offsets[i];

                        CanvasImage icon;
                        if (offset > 0) {
                            icon = TardisCanvasUtils.getSprite("offset_right");
                        } else if (offset < 0) {
                            icon = TardisCanvasUtils.getSprite("offset_left");
                        } else {
                            icon = TardisCanvasUtils.getSprite("offset_centered");
                        }

                        CanvasUtils.draw(canvas, 8 + i / 2 * 10, 24 + i % 2 * 6, icon);
                    }

                    CanvasUtils.draw(canvas, 7 + state.scaleState * 10, 23, TardisCanvasUtils.getSprite("offset_set_selected"));

                    DefaultFonts.VANILLA.drawText(canvas, "ER: " + state.getDistance(), 8, 41, 8, CanvasColor.WHITE_HIGH);
                });

                tardis.getState(RefuelingState.class).ifPresent(state -> {
                    etaRandom.setSeed(time / 40);

                    var seconds = 1000 - tardis.getFuel() * etaRandom.nextBetween(99, 101) / 100;
                    var minutes = seconds / 60;
                    seconds %= 60;

                    DefaultFonts.VANILLA.drawText(canvas, "Time to full:", 8, 24, 8, CanvasColor.LIGHT_GRAY_HIGH);
                    DefaultFonts.VANILLA.drawText(canvas, "%d:%02d".formatted(minutes, seconds), 8, 35, 8, CanvasColor.WHITE_HIGH);
                });

                var random = blockEntity.drawRandom;
                var state = tardis.getState();
                var isSolid = state.isSolid(tardis);
                var stutterOffsetStability = isSolid ? 0 : random.nextBetween(-1, 1);

                int waveOffsetStability = 0;
                if (!isSolid) {
                    var wave1 = (Math.sin(time / 30.0) - 1) * 2.5;
                    var wave2 = (Math.sin(time / 13.0 + 0.5) - 1) * 1.5;
                    var wave3 = (Math.sin(time / 8.0 + 0.3) - 1) * 1;
                    waveOffsetStability = (int) (wave1 + wave2 + wave3);
                }

                drawVerticalBar(canvas, tardis.getStability() * 480 / 10000 + stutterOffsetStability + waveOffsetStability, 96, 16, TardisCanvasUtils.getSprite("vertical_bar_orange"), "STB");
                var stutterOffsetFuel = isSolid || (state instanceof RefuelingState && tardis.getFuel() < 1000) ? 0 : random.nextBetween(-1, 1);
                drawVerticalBar(canvas, tardis.getFuel() * 480 / 10000 + stutterOffsetFuel, 72, 16, TardisCanvasUtils.getSprite("vertical_bar_blue"), "ART");

                var conduitsUnlocked = controls.areEnergyConduitsUnlocked();
                CanvasUtils.draw(canvas, 24, 48, conduitsUnlocked ? TardisCanvasUtils.getSprite("energy_conduits_active") : TardisCanvasUtils.getSprite("energy_conduits_inactive"));
                DefaultFonts.VANILLA.drawText(canvas, "CND", 24 + 7, 80, 8, CanvasColor.WHITE_HIGH);

                var destinationLocked = controls.isDestinationLocked();
                CanvasUtils.draw(canvas, 0, 48, destinationLocked ? TardisCanvasUtils.getSprite("lock_icon_locked") : TardisCanvasUtils.getSprite("lock_icon_unlocked"));
                DefaultFonts.VANILLA.drawText(canvas, "LCK", 7, 80, 8, CanvasColor.WHITE_HIGH);
            }

            @Override
            public void screenTick(ScreenBlockEntity blockEntity) {
                controls.getTardis().getState(FlyingState.class).ifPresentOrElse(state -> {
                    for (int i = 0; i < lastOffsets.length; i++) {
                        int last = lastOffsets[i];
                        int current = state.offsets[i];
                        if (current != last) {
                            lastOffsets[i] = current;
                            if (current == 0) {
                                blockEntity.playSound(SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), 1, 1);
                            }
                        }
                    }
                }, () -> {
                    Arrays.fill(lastOffsets, 0);
                });
            }

            @Override
            public void drawBackground(ScreenBlockEntity blockEntity, DrawableCanvas canvas) {
                CanvasUtils.draw(canvas, 0, 0, TardisCanvasUtils.getSprite("status_background"));
            }

            @Override
            public boolean onClick(ScreenBlockEntity blockEntity, ServerPlayerEntity player, ClickType type, int x, int y) {
                return false;
            }
        };
    }

    private void drawVerticalBar(DrawableCanvas canvas, int value, int x, int y, CanvasImage barType, String label) {
        CanvasUtils.draw(canvas, x, y, TardisCanvasUtils.getSprite("vertical_bar_empty"));
        for (int ly = 48 - value; ly < 48; ly++) {
            for (int lx = 0; lx < 16; lx++) {
                byte color = barType.getRaw(8 + lx, 8 + ly);

                if (color != 0) {
                    canvas.setRaw(8 + lx + x, 8 + ly + y, color);
                }
            }
        }

        var labelWidth = DefaultFonts.VANILLA.getTextWidth(label, 8);
        DefaultFonts.VANILLA.drawText(canvas, label, x + 15 - labelWidth / 2, y + 64, 8, CanvasColor.WHITE_HIGH);
    }

    @Override
    public void drawIcon(TardisControl controls, ScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        CanvasUtils.draw(canvas, 0, 0, TardisCanvasUtils.getSprite("app/status"));
    }

    @Override
    public ScreenAppType<?> getType() {
        return ScreenAppTypes.STATUS;
    }
}
