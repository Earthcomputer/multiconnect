package net.earthcomputer.multiconnect.protocols.v1_12_1;

import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.earthcomputer.multiconnect.protocols.v1_12_2.Protocol_1_12_2;
import net.earthcomputer.multiconnect.transformer.VarInt;
import net.minecraft.client.network.packet.KeepAliveS2CPacket;
import net.minecraft.server.network.packet.KeepAliveC2SPacket;

import java.util.function.Supplier;

public class Protocol_1_12_1 extends Protocol_1_12_2 {

    public static void registerTranslators() {
        ProtocolRegistry.registerInboundTranslator(KeepAliveS2CPacket.class, buf -> {
            buf.pendingRead(Long.class, (long) buf.readVarInt());
            buf.applyPendingReads();
        });

        ProtocolRegistry.registerOutboundTranslator(KeepAliveC2SPacket.class, buf -> {
            Supplier<Long> id = buf.skipWrite(Long.class);
            buf.pendingWrite(VarInt.class, () -> new VarInt(id.get().intValue()), val -> buf.writeVarInt(val.get()));
        });
    }

}
