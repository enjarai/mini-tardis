package dev.enjarai.minitardis.component.flight;

import com.mojang.serialization.MapCodec;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.ModSounds;
import dev.enjarai.minitardis.block.console.ScreenBlockEntity;
import dev.enjarai.minitardis.component.Tardis;
import dev.enjarai.minitardis.component.TardisControl;
import dev.enjarai.minitardis.component.screen.canvas.CanvasColors;
import dev.enjarai.minitardis.component.screen.canvas.patbox.DrawableCanvas;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

public class DisabledState implements FlightState {
    public static final MapCodec<DisabledState> CODEC = MapCodec.unit(DisabledState::new);
    public static final Identifier ID = MiniTardis.id("disabled");

    @Override
    public FlightState tick(Tardis tardis) {
        return this;
    }

    @Override
    public void init(Tardis tardis) {
        playForInterior(tardis, SoundEvents.BLOCK_RESPAWN_ANCHOR_DEPLETE.value(), SoundCategory.BLOCKS, 2, 0);
        playForInterior(tardis, SoundEvents.BLOCK_BEACON_DEACTIVATE, SoundCategory.BLOCKS, 2, 0);
        stopPlayingForInterior(tardis, ModSounds.CORAL_HUM);
    }

    @Override
    public boolean suggestTransition(Tardis tardis, FlightState newState) {
        return newState instanceof BootingUpState;
    }

    @Override
    public boolean isInteriorLightEnabled(int order) {
        return false;
    }

    @Override
    public boolean tryChangeCourse(Tardis tardis) {
        return false;
    }

    @Override
    public boolean isPowered(Tardis tardis) {
        return false;
    }

//    @Override
//    public boolean overrideScreenImage(Tardis tardis) {
//        return true;
//    }
//
//    @Override
//    public void drawScreenImage(TardisControl controls, DrawableCanvas canvas, ScreenBlockEntity blockEntity) {
//        canvas.fillRaw(CanvasColors.BLACK);
//    }

    @Override
    public Identifier id() {
        return ID;
    }
}
