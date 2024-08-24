package dev.enjarai.minitardis.net;

import dev.enjarai.minitardis.MiniTardis;
import eu.pb4.polymer.networking.api.ContextByteBuf;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import xyz.nucleoid.packettweaker.PacketContext;

public record ICanHasMiniTardisPayload() implements CustomPayload {


    public static final PacketCodec<ContextByteBuf, ICanHasMiniTardisPayload> PACKET_CODEC = PacketCodec.of(
            ICanHasMiniTardisPayload::read,
            ICanHasMiniTardisPayload::write
    );

    private void read(ByteBuf byteBuf) {

    }

    private static ICanHasMiniTardisPayload write(ByteBuf byteBuf) {
        return new ICanHasMiniTardisPayload();
    }


    @Override
    public Id<? extends CustomPayload> getId() {
        //DUMMY CLASS TO FOOL POLYMER HEHEHEHEHEHEHHE
        return null;
    }
}
