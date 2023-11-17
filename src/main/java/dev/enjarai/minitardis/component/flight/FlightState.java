package dev.enjarai.minitardis.component.flight;

import com.mojang.serialization.Codec;
import dev.enjarai.minitardis.component.Tardis;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Map;

public interface FlightState {
    Map<Identifier, Codec<? extends FlightState>> ALL = Map.of(
            LandedState.ID, LandedState.CODEC,
            TakingOffState.ID, TakingOffState.CODEC,
            FlyingState.ID, FlyingState.CODEC,
            LandingState.ID, LandingState.CODEC,
            SearchingForLandingState.ID, SearchingForLandingState.CODEC
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
    default boolean suggestTransition(Tardis tardis, FlightState newState) {
        return true;
    }

    /**
     * Whether entities should be able to enter and exit the Tardis.
     */
    default boolean isSolid(Tardis tardis) {
        return true;
    }

    /**
     * Whether the destination of the Tardis can be changed during this state.
     */
    default boolean canChangeCourse(Tardis tardis) {
        return true;
    }

    /**
     * A unique id for serialization
     */
    Identifier id();

    default Text getName() {
        return Text.translatable("mini_tardis.state." + id().getNamespace() + "." + id().getPath());
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
            world.playSound(null, tardis.getCurrentLocation().get().pos(), soundEvent, category, volume, pitch);
        });
    }

    default void tickScreenShake(Tardis tardis, float intensity) {
        var world = tardis.getInteriorWorld();
        var random = world.getRandom();
        for (var player : world.getPlayers()) {
            var amountX = random.nextFloat() * intensity - intensity / 2;
            var amountY = random.nextFloat() * intensity - intensity / 2;

            player.teleport(
                    world, player.getX(), player.getY(), player.getZ(),
                    PositionFlag.VALUES,
                    player.getYaw() + amountX, player.getPitch() + amountY
            );
        }
    }
}
