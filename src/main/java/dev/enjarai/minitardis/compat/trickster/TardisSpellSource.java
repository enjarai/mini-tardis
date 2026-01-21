package dev.enjarai.minitardis.compat.trickster;

import dev.enjarai.minitardis.component.Tardis;
import dev.enjarai.trickster.spell.Fragment;
import dev.enjarai.trickster.spell.execution.source.SpellSource;
import dev.enjarai.trickster.spell.mana.ManaPool;
import dev.enjarai.trickster.spell.mana.ManaPoolType;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.ComponentKey;

import java.util.Optional;

public class TardisSpellSource extends SpellSource {
    public final Tardis tardis;
    public final GuardingSpellApp app;

    public TardisSpellSource(Tardis tardis, GuardingSpellApp app) {
        this.tardis = tardis;
        this.app = app;
    }

    @Override
    public ManaPool getManaPool() {
        return new ManaPool() {
            @Override
            @Nullable
            public ManaPoolType<?> type() {
                return null;
            }

            @Override
            public void set(float value) {
                tardis.addOrDrainFuel(Math.round(value));
            }

            @Override
            public float get() {
                return tardis.getFuel();
            }

            @Override
            public float getMax() {
                return 1000;
            }
        };
    }

    @Override
    public <T extends Component> Optional<T> getComponent(ComponentKey<T> key) {
        return Optional.empty();
    }

    @Override
    public float getHealth() {
        return 30;
    }

    @Override
    public float getMaxHealth() {
        return 30;
    }

    @Override
    public Vector3d getPos() {
        return tardis.getCurrentLandedLocation()
                .orElseThrow(() -> new IllegalStateException("Spell attempted to acquire TARDIS location while not landed; spell execution is not permitted during flight"))
                .pos()
                .toCenterPos()
                .toVector3d();
    }

    @Override
    public ServerWorld getWorld() {
        return tardis.getExteriorWorld()
                .orElseThrow(() -> new IllegalStateException("Spell attempted to acquire TARDIS exterior world while not landed; spell execution is not permitted during flight"));
    }

    @Override
    public Fragment getCrowMind() {
        return app.crowMind;
    }

    @Override
    public void setCrowMind(Fragment fragment) {
        app.crowMind = fragment;
    }
}
