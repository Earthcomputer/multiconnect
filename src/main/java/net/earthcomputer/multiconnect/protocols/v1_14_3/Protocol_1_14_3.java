package net.earthcomputer.multiconnect.protocols.v1_14_3;

import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.earthcomputer.multiconnect.protocols.generic.PacketInfo;
import net.earthcomputer.multiconnect.protocols.v1_14_4.Protocol_1_14_4;
import net.minecraft.network.packet.s2c.play.SetTradeOffersS2CPacket;

import java.util.List;

public class Protocol_1_14_3 extends Protocol_1_14_4 {

    public static void registerTranslators() {
        ProtocolRegistry.registerInboundTranslator(SetTradeOffersS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            buf.readVarInt(); // sync id
            int count = buf.readByte() & 255;
            for (int i = 0; i < count; i++) {
                buf.readItemStack(); // first buy item
                buf.readItemStack(); // sell item
                if (buf.readBoolean()) { // has second buy item
                    buf.readItemStack(); // sell item
                }
                buf.readBoolean(); // clear trades
                buf.readInt(); // uses
                buf.readInt(); // max uses
                buf.readInt(); // trader experience
                buf.readInt(); // special price
                buf.readFloat(); // price multiplier
                buf.disablePassthroughMode();
                buf.pendingRead(Integer.class, 0); // demand bonus
                buf.enablePassthroughMode();
            }
            buf.disablePassthroughMode();
            buf.applyPendingReads();
        });
    }

    @Override
    public List<PacketInfo<?>> getClientboundPackets() {
        List<PacketInfo<?>> packets = super.getClientboundPackets();
        packets.remove(packets.size() - 1); // BlockPlayerActionS2CPacket
        return packets;
    }

}
