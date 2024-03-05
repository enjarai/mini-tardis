package dev.enjarai.minitardis.net;

import dev.enjarai.minitardis.MiniTardis;
import eu.pb4.polymer.networking.api.payload.VersionedPayload;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import xyz.nucleoid.packettweaker.PacketContext;

public record ICanHasMiniTardisPayload() implements VersionedPayload {

    @Override
    public void write(PacketContext context, int version, PacketByteBuf buf) {
    }

    @Override
    public Identifier id() {
        return MiniTardis.HANDSHAKE_CHANNEL;
    }

    public static ICanHasMiniTardisPayload read(PacketContext context, Identifier identifier, int version, PacketByteBuf buf) {
        return new ICanHasMiniTardisPayload();
    }
}
