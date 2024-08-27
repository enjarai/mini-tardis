package dev.enjarai.minitardis.component.flight;

import com.mojang.serialization.MapCodec;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.ModSounds;
import dev.enjarai.minitardis.block.console.ScreenBlockEntity;
import dev.enjarai.minitardis.canvas.TardisCanvasUtils;
import dev.enjarai.minitardis.component.Tardis;
import dev.enjarai.minitardis.component.TardisControl;
import eu.pb4.mapcanvas.api.core.CanvasColor;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;

public class CrashedState implements FlightState {
    public static final MapCodec<CrashedState> CODEC = MapCodec.unit(CrashedState::new);
    public static final Identifier ID = MiniTardis.id("crashed");

    private int litLamps;

    @Override
    public FlightState tick(Tardis tardis) {
        if (tardis.getInteriorWorld().getTime() % 100 == 0) {
            playForInteriorAndExterior(tardis, ModSounds.CLOISTER_BELL, SoundCategory.BLOCKS, 1, 1);
        }

        if (tardis.getInteriorWorld().getRandom().nextInt(20) == 0) {
            var size = tardis.getInteriorWorld().getRandom().nextInt(6);
            var shift = tardis.getInteriorWorld().getRandom().nextInt(24);
            litLamps = Integer.MAX_VALUE >> (32 - size) << shift;
        }

        return this;
    }

    @Override
    public void init(Tardis tardis) {
        stopPlayingForInterior(tardis, ModSounds.CORAL_HUM);
    }

    @Override
    public boolean suggestTransition(Tardis tardis, FlightState newState) {
        return newState instanceof DisabledState;
    }

    @Override
    public boolean isInteriorLightEnabled(int order) {
        return (litLamps & (1 << order)) > 0;
    }

    @Override
    public boolean tryChangeCourse(Tardis tardis) {
        return false;
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
        var cycle = controls.getTardis().getInteriorWorld().getTime() / 20 % 2;
        CanvasUtils.draw(canvas, 0, 0, TardisCanvasUtils.getSprite("critical_failure_" + cycle));

        TardisCanvasUtils.drawCenteredText(canvas, "CRITICAL FAILURE", 64, 36, CanvasColor.BRIGHT_RED_HIGH);
        TardisCanvasUtils.drawCenteredText(canvas, "Reset power coupling", 64, 46, CanvasColor.RED_HIGH);
        TardisCanvasUtils.drawCenteredText(canvas, "for system reboot", 64, 54, CanvasColor.RED_HIGH);
    }

    @Override
    public Identifier id() {
        return ID;
    }
}
