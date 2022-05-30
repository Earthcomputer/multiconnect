package net.earthcomputer.multiconnect.packets.v1_12_2;

import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.Length;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketCustomPayload;
import net.earthcomputer.multiconnect.packets.SPacketSetTradeOffers;

import java.util.List;

@Polymorphic
@MessageVariant(maxVersion = Protocols.V1_12_2)
public abstract class SPacketCustomPayload_1_12_2 implements SPacketCustomPayload {
    public String channel;

    @Polymorphic(stringValue = "MC|Brand")
    @MessageVariant(maxVersion = Protocols.V1_12_2)
    public static class BrandPayload extends SPacketCustomPayload_1_12_2 implements SPacketCustomPayload.Brand {
        public String brand;
    }

    @Polymorphic(stringValue = "MC|TrList")
    @MessageVariant(maxVersion = Protocols.V1_12_2)
    public static class TraderList extends SPacketCustomPayload_1_12_2 implements SPacketCustomPayload.TraderList {
        public int syncId;
        @Length(type = Types.UNSIGNED_BYTE)
        public List<SPacketSetTradeOffers.Trade> trades;
    }

    @Polymorphic(stringValue = "MC|BOpen")
    @MessageVariant(maxVersion = Protocols.V1_12_2)
    public static class OpenBook extends SPacketCustomPayload_1_12_2 implements SPacketCustomPayload.OpenBook {
        public CommonTypes.Hand hand;
    }

    @Polymorphic(otherwise = true)
    @MessageVariant(maxVersion = Protocols.V1_12_2)
    public static class OtherPayload extends SPacketCustomPayload_1_12_2 implements SPacketCustomPayload.Other {
        @Length(remainingBytes = true)
        public byte[] payload;

        @Handler
        public static void handle() {
            // TODO: 1.12.2 custom payload handling
        }
    }
}
