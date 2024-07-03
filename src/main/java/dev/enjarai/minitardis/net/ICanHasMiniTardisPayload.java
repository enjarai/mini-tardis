package dev.enjarai.minitardis.net;

import dev.enjarai.minitardis.MiniTardis;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;

public class ICanHasMiniTardisPayload implements CustomPayload {

    public static ICanHasMiniTardisPayload read(PacketByteBuf buf) {
        return new ICanHasMiniTardisPayload();
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return MiniTardis.HANDSHAKE_PAYLOAD_ID;
    }
}
