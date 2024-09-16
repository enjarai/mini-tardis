package dev.enjarai.minitardis.component.flight;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.enjarai.minitardis.block.console.ScreenBlockEntity;
import dev.enjarai.minitardis.component.Tardis;
import dev.enjarai.minitardis.component.TardisControl;
import dev.enjarai.minitardis.component.screen.canvas.patbox.DrawableCanvas;
import net.minecraft.network.packet.s2c.play.StopSoundS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.function.Supplier;

public interface FlightState {
    Map<Identifier, MapCodec<? extends FlightState>> ALL = Map.ofEntries(
            Map.entry(LandedState.ID, LandedState.CODEC),
            Map.entry(TakingOffState.ID, TakingOffState.CODEC),
            Map.entry(FlyingState.ID, FlyingState.CODEC),
            Map.entry(LandingState.ID, LandingState.CODEC),
            Map.entry(SearchingForLandingState.ID, SearchingForLandingState.CODEC),
            Map.entry(CrashingState.ID, CrashingState.CODEC),
            Map.entry(RefuelingState.ID, RefuelingState.CODEC),
            Map.entry(DriftingState.ID, DriftingState.CODEC),
            Map.entry(DisabledState.ID, DisabledState.CODEC),
            Map.entry(BootingUpState.ID, BootingUpState.CODEC),
            Map.entry(CrashedState.ID, CrashedState.CODEC),
            Map.entry(SuspendedFlightState.ID, SuspendedFlightState.CODEC),
            Map.entry(InterdictingState.ID, InterdictingState.CODEC),
            Map.entry(BeingInterdictedState.ID, BeingInterdictedState.CODEC)
    );
    Map<Identifier, Supplier<? extends FlightState>> CONSTRUCTORS = Map.of(
            LandedState.ID, LandedState::new,
            RefuelingState.ID, RefuelingState::new,
            DriftingState.ID, DriftingState::new,
            DisabledState.ID, DisabledState::new,
            BootingUpState.ID, BootingUpState::new,
            CrashedState.ID, CrashedState::new,
            SuspendedFlightState.ID, SuspendedFlightState::new
    );
    Codec<FlightState> CODEC = Identifier.CODEC.dispatch(FlightState::id, ALL::get);

    /**
     * Called once when this state is transitioned into.
     */
    default void init(Tardis tardis) {
    }

    /**
     * Called every tick that this state is active, may return another state
     * instance to transition to another state, or itself.
     */
    FlightState tick(Tardis tardis);

    /**
     * Called once when this state is transitioned out of.
     */
    default void complete(Tardis tardis) {
    }

    /**
     * External factors may use this method to suggest a state transition,
     * the implementor can return a boolean to accept or reject this.
     */
    boolean suggestTransition(Tardis tardis, FlightState newState);

    /**
     * Whether entities should be able to enter and exit the Tardis.
     */
    default boolean isSolid(Tardis tardis) {
        return true;
    }

    /**
     * Whether the destination of the Tardis can be changed during this state.
     */
    default boolean tryChangeCourse(Tardis tardis) {
        return true;
    }

    /**
     * Whether this state should count as the Tardis being powered on. Controls effects like interior hum.
     */
    default boolean isPowered(Tardis tardis) {
        return true;
    }

    /**
     * Whether the screen should delegate to this state's special drawing function.
     * This lets us do things like show an error in the crashed state, or a loading screen during boot.
     */
    default boolean overrideScreenImage(Tardis tardis) {
        return false;
    }

    default boolean canBeInterdicted(Tardis tardis) {
        return false;
    }

    /**
     * The function that will be used to draw the custom screen image if `overrideScreenImage` returns true.
     */
    default void drawScreenImage(TardisControl controls, DrawableCanvas canvas, ScreenBlockEntity blockEntity) {
    }

    /**
     * Returns the current screen shake intensity inside the Tardis every tick.
     */
    default float getScreenShakeIntensity(Tardis tardis) {
        return 0;
    }

    /**
     * A unique id for serialization
     */
    Identifier id();

    default Text getName() {
        return Text.translatable("mini_tardis.state." + id().getNamespace() + "." + id().getPath());
    }

    /**
     * The transparency level of the Tardis exterior from 15 to 0. -1 is no transparency.
     */
    default byte getExteriorAlpha(Tardis tardis) {
        return -1;
    }

    default boolean isInteriorLightEnabled(int order) {
        return true;
    }

    default void playForInterior(Tardis tardis, SoundEvent soundEvent, SoundCategory category, float volume, float pitch) {
        var world = tardis.getInteriorWorld();
        for (var player : world.getPlayers()) {
            world.playSoundFromEntity(null, player, soundEvent, category, volume, pitch);
        }
    }

    default void playForInteriorAndExterior(Tardis tardis, SoundEvent soundEvent, SoundCategory category, float volume, float pitch) {
        playForInterior(tardis, soundEvent, category, volume, pitch);
        tardis.getExteriorWorld().ifPresent(world -> {
            // We can safely assume currentLocation exists, because we wouldn't have an exterior world if it didn't.
            //noinspection OptionalGetWithoutIsPresent
            world.playSound(null, tardis.getCurrentLandedLocation().get().pos(), soundEvent, category, volume, pitch);
        });
    }

    default void stopPlayingForInterior(Tardis tardis, SoundEvent soundEvent) {
        StopSoundS2CPacket stopSoundS2CPacket = new StopSoundS2CPacket(soundEvent.getId(), null);

        for (var player : tardis.getInteriorWorld().getPlayers()) {
            player.networkHandler.sendPacket(stopSoundS2CPacket);
        }
    }
}
