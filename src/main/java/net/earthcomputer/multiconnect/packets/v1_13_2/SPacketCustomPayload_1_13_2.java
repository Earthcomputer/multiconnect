package net.earthcomputer.multiconnect.packets.v1_13_2;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.Length;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketCustomPayload;
import net.earthcomputer.multiconnect.packets.SPacketOpenWrittenBook;
import net.earthcomputer.multiconnect.packets.SPacketSetTradeOffers;
import net.earthcomputer.multiconnect.packets.v1_14_2.SPacketSetTradeOffers_1_14_2;
import net.minecraft.util.Identifier;

import java.util.List;

@Polymorphic
@MessageVariant(maxVersion = Protocols.V1_13_2)
public abstract class SPacketCustomPayload_1_13_2 implements SPacketCustomPayload {
    public Identifier channel;

    @Polymorphic(stringValue = "brand")
    @MessageVariant(maxVersion = Protocols.V1_13_2)
    public static class BrandPayload extends SPacketCustomPayload_1_13_2 implements Brand {
        public String brand;
    }

    @Polymorphic(stringValue = "trader_list")
    @MessageVariant(maxVersion = Protocols.V1_13_2)
    public static class TraderList extends SPacketCustomPayload_1_13_2 implements SPacketCustomPayload.TraderList {
        public int syncId;
        @Length(type = Types.UNSIGNED_BYTE)
        public List<SPacketSetTradeOffers.Trade> trades;

        @Handler
        public static SPacketSetTradeOffers_1_14_2 handle(
                @Argument("syncId") int syncId,
                @Argument("trades") List<SPacketSetTradeOffers.Trade> trades
        ) {
            var packet = new SPacketSetTradeOffers_1_14_2();
            packet.syncId = syncId;
            packet.trades = trades;
            packet.villagerLevel = 5;
            packet.experience = 0;
            packet.isRegularVillager = false;
            return packet;
        }
    }

    @Polymorphic(stringValue = "open_book")
    @MessageVariant(maxVersion = Protocols.V1_13_2)
    public static class OpenBook extends SPacketCustomPayload_1_13_2 implements SPacketCustomPayload.OpenBook {
        public CommonTypes.Hand hand;

        @Handler
        public static SPacketOpenWrittenBook handle(@Argument("hand") CommonTypes.Hand hand) {
            var packet = new SPacketOpenWrittenBook();
            packet.hand = hand;
            return packet;
        }
    }

    @Polymorphic(otherwise = true)
    @MessageVariant(maxVersion = Protocols.V1_13_2)
    public static class OtherPayload extends SPacketCustomPayload_1_13_2 implements Other {
        @Length(remainingBytes = true)
        public byte[] payload;
    }
}
