package dev.enjarai.minitardis.component.screen.app;

import com.mojang.serialization.Codec;
import dev.enjarai.minitardis.block.console.ConsoleScreenBlockEntity;
import dev.enjarai.minitardis.canvas.TardisCanvasUtils;
import dev.enjarai.minitardis.component.TardisControl;
import dev.enjarai.minitardis.component.flight.DriftingState;
import dev.enjarai.minitardis.component.flight.FlyingState;
import dev.enjarai.minitardis.component.flight.RefuelingState;
import eu.pb4.mapcanvas.api.core.CanvasColor;
import eu.pb4.mapcanvas.api.core.CanvasImage;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.api.font.DefaultFonts;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ClickType;

public class StatusApp implements ScreenApp {
    public static final Codec<StatusApp> CODEC = Codec.unit(StatusApp::new);

    @Override
    public AppView getView(TardisControl controls) {
        return new AppView() {
            @Override
            public void draw(ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas) {
                var tardis = controls.getTardis();

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

                var random = blockEntity.drawRandom;
                var state = tardis.getState();
                var isSolid = state.isSolid(tardis);
                var stutterOffsetStability = isSolid ? 0 : random.nextBetween(-1, 1);
                drawVerticalBar(canvas, tardis.getStability() * 480 / 10000 + stutterOffsetStability, 96, 16, TardisCanvasUtils.getSprite("vertical_bar_orange"), "STB");
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
            public void drawBackground(ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas) {
                CanvasUtils.draw(canvas, 0, 0, TardisCanvasUtils.getSprite("status_background"));
            }

            @Override
            public boolean onClick(ConsoleScreenBlockEntity blockEntity, ServerPlayerEntity player, ClickType type, int x, int y) {
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
    public void drawIcon(TardisControl controls, ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        CanvasUtils.draw(canvas, 0, 0, TardisCanvasUtils.getSprite("app/status"));
    }

    @Override
    public ScreenAppType<?> getType() {
        return ScreenAppTypes.STATUS;
    }
}
