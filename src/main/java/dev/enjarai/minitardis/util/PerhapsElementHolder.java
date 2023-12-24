package dev.enjarai.minitardis.util;

import dev.enjarai.minitardis.MiniTardis;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import net.minecraft.server.network.ServerPlayNetworkHandler;

public class PerhapsElementHolder extends ElementHolder {
    @Override
    public boolean startWatching(ServerPlayNetworkHandler player) {
        if (MiniTardis.playerIsRealGamer(player)) {
            return false;
        }

        return super.startWatching(player);
    }
}
