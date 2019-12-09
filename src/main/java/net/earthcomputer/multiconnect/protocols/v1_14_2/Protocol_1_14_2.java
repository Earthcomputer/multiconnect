package net.earthcomputer.multiconnect.protocols.v1_14_2;

import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.earthcomputer.multiconnect.protocols.v1_14_3.Protocol_1_14_3;
import net.minecraft.client.network.packet.SetTradeOffersPacket;
import net.minecraft.village.TraderOfferList;

public class Protocol_1_14_2 extends Protocol_1_14_3 {

    @Override
    public void registerTranslators() {
        ProtocolRegistry.registerInboundTranslator(SetTradeOffersPacket.class, buf -> {
            buf.enablePassthroughMode();
            buf.readVarInt();
            TraderOfferList.fromPacket(buf);
            buf.readVarInt();
            buf.readVarInt();
            buf.readBoolean();
            buf.disablePassthroughMode();
            buf.pendingRead(Boolean.class, true);
            buf.applyPendingReads();
        });
    }
}
