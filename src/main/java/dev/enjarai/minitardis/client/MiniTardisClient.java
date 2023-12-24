package dev.enjarai.minitardis.client;

import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.net.HandshakeClient;
import net.fabricmc.api.ClientModInitializer;

public class MiniTardisClient implements ClientModInitializer {
    public static final HandshakeClient<Unit> HANDSHAKE_CLIENT = new HandshakeClient<>(
            Codec.unit(Unit.INSTANCE), MiniTardis.HANDSHAKE_CHANNEL, (unit) -> {});

    @Override
    public void onInitializeClient() {

    }
}
