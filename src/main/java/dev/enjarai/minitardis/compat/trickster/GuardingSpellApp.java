package dev.enjarai.minitardis.compat.trickster;

import com.mojang.serialization.MapCodec;
import dev.enjarai.minitardis.block.console.ScreenBlockEntity;
import dev.enjarai.minitardis.canvas.TardisCanvasUtils;
import dev.enjarai.minitardis.component.Tardis;
import dev.enjarai.minitardis.component.TardisControl;
import dev.enjarai.minitardis.component.screen.app.*;
import dev.enjarai.minitardis.component.screen.canvas.CanvasColors;
import dev.enjarai.minitardis.component.screen.canvas.patbox.DrawableCanvas;
import dev.enjarai.trickster.spell.Fragment;
import dev.enjarai.trickster.spell.SpellPart;
import dev.enjarai.trickster.spell.execution.executor.DefaultSpellExecutor;
import dev.enjarai.trickster.spell.fragment.VoidFragment;
import io.wispforest.accessories.endec.CodecUtils;
import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.StructEndecBuilder;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ClickType;

import java.util.List;
import java.util.Optional;

public class GuardingSpellApp implements ScreenApp {
    public static final Endec<GuardingSpellApp> ENDEC = StructEndecBuilder.of(
            DefaultSpellExecutor.ENDEC.optionalOf().fieldOf("spell", app -> Optional.of(app.spell)),
            Fragment.ENDEC.optionalOf().fieldOf("crow_mind", app -> Optional.of(app.crowMind)),
            GuardingSpellApp::new
    );
    public static final ScreenAppType<GuardingSpellApp> TYPE =
            new ScreenAppType<>(MapCodec.assumeMapUnsafe(CodecUtils.ofEndec(GuardingSpellApp.ENDEC)), GuardingSpellApp::new, false);

    public final DefaultSpellExecutor spell;
    public Fragment crowMind;

    public GuardingSpellApp(Optional<DefaultSpellExecutor> spell, Optional<Fragment> crowMind) {
        this.spell = spell.orElse(new DefaultSpellExecutor(new SpellPart(), List.of()));
        this.crowMind = crowMind.orElse(VoidFragment.INSTANCE);
    }

    public GuardingSpellApp() {
        this(Optional.empty(), Optional.empty());
    }

    @Override
    public AppView getView(TardisControl controls) {
        return new AppView() {
            @Override
            public void draw(ScreenBlockEntity blockEntity, DrawableCanvas canvas) {
                TardisCanvasUtils.drawCenteredText(canvas, "This app does not implement a screen", 64, 40, CanvasColors.WHITE);
            }

            @Override
            public boolean onClick(ScreenBlockEntity blockEntity, ServerPlayerEntity player, ClickType type, int x, int y) {
                return false;
            }
        };
    }

    @Override
    public void tardisTick(Tardis tardis) {
        var state = tardis.getState();

        if (state.isPowered(tardis) && state.isSolid(tardis)) {
            spell.run(new TardisSpellSource(tardis, this));
        }
    }

    @Override
    public ScreenAppType<?> getType() {
        return TYPE;
    }
}
