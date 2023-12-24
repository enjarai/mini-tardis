package dev.enjarai.minitardis.util;

import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.net.HandshakeServer;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import net.minecraft.server.network.ServerPlayNetworkHandler;

public class PerhapsElementHolder extends ElementHolder {
    @Override
    public boolean startWatching(ServerPlayNetworkHandler player) {
        if (MiniTardis.HANDSHAKE_SERVER.getHandshakeState(player) == HandshakeServer.HandshakeState.ACCEPTED) {
            return false;
        }

        return super.startWatching(player);
    }
}
