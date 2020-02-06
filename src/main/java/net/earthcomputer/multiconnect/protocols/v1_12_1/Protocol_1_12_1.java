package net.earthcomputer.multiconnect.protocols.v1_12_1;

import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.earthcomputer.multiconnect.protocols.v1_12_2.Protocol_1_12_2;
import net.earthcomputer.multiconnect.transformer.VarInt;
import net.minecraft.network.packet.c2s.play.KeepAliveC2SPacket;
import net.minecraft.network.packet.s2c.play.KeepAliveS2CPacket;

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
