package net.earthcomputer.multiconnect.protocols.v1_14_2;

import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.earthcomputer.multiconnect.protocols.v1_14_3.Protocol_1_14_3;
import net.minecraft.item.MerchantOffers;
import net.minecraft.network.play.server.SMerchantOffersPacket;

public class Protocol_1_14_2 extends Protocol_1_14_3 {

    public static void registerTranslators() {
        ProtocolRegistry.registerInboundTranslator(SMerchantOffersPacket.class, buf -> {
            buf.enablePassthroughMode();
            buf.readVarInt();
            MerchantOffers.read(buf);
            buf.readVarInt();
            buf.readVarInt();
            buf.readBoolean();
            buf.disablePassthroughMode();
            buf.pendingRead(Boolean.class, true);
            buf.applyPendingReads();
        });
    }
}
