package dev.enjarai.minitardis.ccacomponent.flight;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.ModSounds;
import dev.enjarai.minitardis.block.console.ScreenBlockEntity;
import dev.enjarai.minitardis.ccacomponent.Tardis;
import dev.enjarai.minitardis.ccacomponent.TardisControl;
import eu.pb4.mapcanvas.api.core.CanvasColor;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.api.font.DefaultFonts;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class BootingUpState extends TransitionalFlightState {
    public static final MapCodec<BootingUpState> CODEC = Codec.INT
            .xmap(BootingUpState::new, s -> s.ticksPassed).fieldOf("ticks_passed");
    public static final Identifier ID = MiniTardis.id("booting_up");

    private final List<Line> consoleLogs = new ArrayList<>();

    private BootingUpState(int ticksPassed) {
        super(ticksPassed);
    }

    public BootingUpState() {
        this(0);
    }

    @Override
    public void complete(Tardis tardis) {
        playForInterior(tardis, SoundEvents.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, SoundCategory.BLOCKS, 2, 0);
        playForInterior(tardis, SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.BLOCKS, 2, 0);
        playForInterior(tardis, ModSounds.CORAL_HUM, SoundCategory.AMBIENT, 0.3f, 1);
    }

    @Override
    public FlightState tick(Tardis tardis) {
        if (ticksPassed % 15 == 0) {
            playForInterior(tardis, SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, SoundCategory.BLOCKS, 1, ticksPassed / 100f);
        }

        writeLineForTick();

        return super.tick(tardis);
    }

    @Override
    public boolean suggestTransition(Tardis tardis, FlightState newState) {
        return false;
    }

    @Override
    public FlightState getNextState(Tardis tardis) {
        return new LandedState();
    }

    @Override
    public int getTransitionDuration(Tardis tardis) {
        return 15 * 12;
    }

    @Override
    public boolean isInteriorLightEnabled(int order) {
        return order <= ticksPassed / 15;
    }

    @Override
    public boolean isPowered(Tardis tardis) {
        return false;
    }

    @Override
    public boolean overrideScreenImage(Tardis tardis) {
        return true;
    }

    @Override
    public void drawScreenImage(TardisControl controls, DrawableCanvas canvas, ScreenBlockEntity blockEntity) {
        CanvasUtils.fill(canvas, 0, 0, 128, 96, CanvasColor.BLACK_HIGH);
        // Iterate only over the last 10 lines to simulate scrolling
        var subList = consoleLogs.subList(Math.max(0, consoleLogs.size() - 10), consoleLogs.size());
        for (int i = 0; i < subList.size(); i++) {
            var line = subList.get(i);
            var y = 2 + i * 9;
            DefaultFonts.VANILLA.drawText(canvas, line.line(), 2, y, 8, CanvasColor.WHITE_HIGH);

            if (line.loads()) {
                var width = DefaultFonts.VANILLA.getTextWidth(line.line(), 8);

                if (i == subList.size() - 1) {
                    DefaultFonts.VANILLA.drawText(canvas, getSpinnyThing(controls.getTardis().getInteriorWorld().getTime()),
                            width + 6, y, 8, CanvasColor.ORANGE_HIGH);
                } else {
                    DefaultFonts.VANILLA.drawText(canvas, "Done", width + 6, y, 8, CanvasColor.LIME_HIGH);
                }
            }
        }
    }

    private String getSpinnyThing(long tick) {
        return switch ((int) (tick / 4 % 4)) {
            case 0 -> "-";
            case 1 -> "\\";
            case 2 -> "|";
            case 3 -> "/";
            default -> throw new IllegalStateException("Unexpected value: " + tick / 4 % 4);
        };
    }

    private void writeLineForTick() {
        var line = switch (ticksPassed) {
            case 1 -> new Line(" ___       __ ", false);
            case 2 -> new Line("  | | \\  _  (_  ", false);
            case 3 -> new Line("  | |_/ (_) __) ", false);
            case 4 -> new Line("Preparing...", true);

            case 20 -> new Line("Connecting artron banks...", true);
            case 25 -> new Line("Calculating offsets...", true);
            case 36 -> new Line("Initializing GTPS...", true);
            case 43 -> new Line("Locating dimensions...", true);
            case 60 -> new Line("Running diagnostics...", true);
            case 85 -> new Line("No problems found.", false);
            case 89 -> new Line("Core systems ready!", false);

            case 90 -> new Line("Connecting conduits...", true);
            case 100 -> new Line("Spinning up...", true);
            case 120 -> new Line("Compensating...", true);
            case 129 -> new Line("Time rotor ready!", false);

            case 130 -> new Line("Starting main OS...", true);
            case 140 -> new Line("Loading packager...", true);
            case 146 -> new Line("Loading waypoints...", true);
            case 154 -> new Line("Loading history...", true);
            case 159 -> new Line("OS ready!", false);
            case 160 -> new Line("Done!", false);

            // max 180
            default -> null;
        };

        if (line != null) {
            consoleLogs.add(line);
        }
    }

    @Override
    public Identifier id() {
        return ID;
    }

    private record Line(String line, boolean loads) {}
}
