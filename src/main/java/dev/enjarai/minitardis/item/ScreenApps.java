package dev.enjarai.minitardis.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.enjarai.minitardis.component.screen.app.ScreenApp;

import java.util.List;

public record ScreenApps(List<ScreenApp> screenApps) {

    public static final Codec<ScreenApps> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(ScreenApp.CODEC).fieldOf("screen_apps").forGetter(ScreenApps::screenApps)
    ).apply(instance, ScreenApps::new));

}
