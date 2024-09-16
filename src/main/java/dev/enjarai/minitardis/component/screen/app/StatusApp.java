package dev.enjarai.minitardis.component.screen.app;

import com.mojang.serialization.Codec;
import dev.enjarai.minitardis.block.console.ScreenBlockEntity;
import dev.enjarai.minitardis.canvas.TardisCanvasUtils;
import dev.enjarai.minitardis.component.TardisControl;
import dev.enjarai.minitardis.component.flight.DriftingState;
import dev.enjarai.minitardis.component.flight.FlyingState;
import dev.enjarai.minitardis.component.flight.InterdictState;
import dev.enjarai.minitardis.component.flight.RefuelingState;
import dev.enjarai.minitardis.component.screen.canvas.CanvasColors;
import dev.enjarai.minitardis.component.screen.canvas.patbox.CanvasImage;
import dev.enjarai.minitardis.component.screen.canvas.patbox.DrawableCanvas;
import dev.enjarai.minitardis.component.screen.canvas.patbox.font.DefaultFonts;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ClickType;
import net.minecraft.util.math.random.LocalRandom;
import net.minecraft.util.math.random.Random;

import java.util.Arrays;

public class StatusApp implements ScreenApp {
    public static final Codec<StatusApp> CODEC = Codec.unit(StatusApp::new);

    private final Random etaRandom = new LocalRandom(0);

    @Override
    public AppView getView(TardisControl controls) {
        return new AppView() {
            final int[] lastOffsets = new int[8];

            @Override
            public void draw(ScreenBlockEntity blockEntity, DrawableCanvas canvas) {
                var tardis = controls.getTardis();
                var time = tardis.getInteriorWorld().getTime();

                DefaultFonts.VANILLA.drawText(canvas, tardis.getState().getName().getString(), 4, 6, 8, CanvasColors.WHITE);

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

                        canvas.draw(8 + i * 8, 24, icon);
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

                        canvas.draw(8 + i / 2 * 10, 24 + i % 2 * 6, icon);
                    }

                    canvas.draw(7 + state.scaleState * 10, 23, TardisCanvasUtils.getSprite("offset_set_selected"));

                    DefaultFonts.VANILLA.drawText(canvas, "ER: " + state.getDistance(), 8, 41, 8, CanvasColors.WHITE);
                });

                tardis.getState(RefuelingState.class).ifPresent(state -> {
                    etaRandom.setSeed(time / 40);

                    var seconds = 1000 - tardis.getFuel() * etaRandom.nextBetween(99, 101) / 100;
                    var minutes = seconds / 60;
                    seconds %= 60;

                    DefaultFonts.VANILLA.drawText(canvas, "Time to full:", 8, 24, 8, CanvasColors.LIGHT_GRAY);
                    DefaultFonts.VANILLA.drawText(canvas, "%d:%02d".formatted(minutes, seconds), 8, 35, 8, CanvasColors.WHITE);
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

                tardis.getState(InterdictState.class).ifPresentOrElse(interdictState -> {
                    canvas.draw(0, 16, TardisCanvasUtils.getSprite("intercept_radar"));

                    var targetX = interdictState.getTargetX();
                    var targetY = interdictState.getTargetY();
                    var currentX = interdictState.getOffsetX();
                    var currentY = interdictState.getOffsetY();
                    canvas.draw(35 + targetX * 6, 16 + 23 + targetY * 6, TardisCanvasUtils.getSprite("intercept_target"));
                    canvas.draw(35 + currentX * 6, 16 + 23 + currentY * 6, TardisCanvasUtils.getSprite("intercept_current"));

                    int phase = interdictState.getPhasesComplete();
                    int otherPhase = interdictState.getLinkedState(tardis).map(InterdictState::getPhasesComplete).orElse(0);
                    for (int i = 0; i < InterdictState.PHASES; i++) {
                        canvas.draw(8 + i * 8, 68, TardisCanvasUtils.getSprite(i < phase ? "intercept_phase_complete" : "intercept_phase"));
                        canvas.draw(8 + i * 8, 76, TardisCanvasUtils.getSprite(i < otherPhase ? "intercept_phase_other_complete" : "intercept_phase_other"));
                    }
                }, () -> {
                    var conduitsUnlocked = controls.areEnergyConduitsUnlocked();
                    canvas.draw(24, 48, conduitsUnlocked ? TardisCanvasUtils.getSprite("energy_conduits_active") : TardisCanvasUtils.getSprite("energy_conduits_inactive"));
                    DefaultFonts.VANILLA.drawText(canvas, "CND", 24 + 7, 80, 8, CanvasColors.WHITE);

                    var destinationLocked = controls.isDestinationLocked();
                    canvas.draw(0, 48, destinationLocked ? TardisCanvasUtils.getSprite("lock_icon_locked") : TardisCanvasUtils.getSprite("lock_icon_unlocked"));
                    DefaultFonts.VANILLA.drawText(canvas, "LCK", 7, 80, 8, CanvasColors.WHITE);
                });
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
                canvas.draw(0, 0, TardisCanvasUtils.getSprite("status_background"));
            }

            @Override
            public boolean onClick(ScreenBlockEntity blockEntity, ServerPlayerEntity player, ClickType type, int x, int y) {
                return false;
            }
        };
    }

    private void drawVerticalBar(DrawableCanvas canvas, int value, int x, int y, CanvasImage barType, String label) {
        canvas.draw(x, y, TardisCanvasUtils.getSprite("vertical_bar_empty"));
        for (int ly = 48 - value; ly < 48; ly++) {
            for (int lx = 0; lx < 16; lx++) {
                short color = barType.getRaw(8 + lx, 8 + ly);

                if (color != 0) {
                    canvas.setRaw(8 + lx + x, 8 + ly + y, color);
                }
            }
        }

        var labelWidth = DefaultFonts.VANILLA.getTextWidth(label, 8);
        DefaultFonts.VANILLA.drawText(canvas, label, x + 15 - labelWidth / 2, y + 64, 8, CanvasColors.WHITE);
    }

    @Override
    public void drawIcon(TardisControl controls, ScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        canvas.draw(0, 0, TardisCanvasUtils.getSprite("app/status"));
    }

    @Override
    public ScreenAppType<?> getType() {
        return ScreenAppTypes.STATUS;
    }
}
