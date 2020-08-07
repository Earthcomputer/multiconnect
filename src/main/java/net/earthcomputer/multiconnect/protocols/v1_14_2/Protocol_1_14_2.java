package net.earthcomputer.multiconnect.protocols.v1_14_2;

import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.earthcomputer.multiconnect.protocols.v1_14_3.Protocol_1_14_3;
import net.minecraft.network.packet.s2c.play.SetTradeOffersS2CPacket;

public class Protocol_1_14_2 extends Protocol_1_14_3 {

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
            }
            buf.readVarInt(); // level progress
            buf.readVarInt(); // experience
            buf.readBoolean(); // leveled
            buf.disablePassthroughMode();
            buf.pendingRead(Boolean.class, true); // refreshable
            buf.applyPendingReads();
        });
    }
}
