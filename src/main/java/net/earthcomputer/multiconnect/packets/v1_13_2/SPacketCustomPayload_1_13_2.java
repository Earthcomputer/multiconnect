package net.earthcomputer.multiconnect.packets.v1_13_2;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.Length;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketCustomPayload;
import net.earthcomputer.multiconnect.packets.SPacketOpenBook;
import net.earthcomputer.multiconnect.packets.SPacketMerchantOffers;
import net.earthcomputer.multiconnect.packets.v1_14_2.SPacketMerchantOffers_1_14_2;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import java.util.List;

@Polymorphic
@MessageVariant(minVersion = Protocols.V1_13, maxVersion = Protocols.V1_13_2)
public abstract class SPacketCustomPayload_1_13_2 implements SPacketCustomPayload {
    public static final ResourceLocation TRADER_LIST = new ResourceLocation("trader_list");
    public static final ResourceLocation OPEN_BOOK = new ResourceLocation("open_book");

    @Introduce(compute = "computeChannel")
    public ResourceLocation channel;

    public static ResourceLocation computeChannel(@Argument("channel") String channel) {
        return switch (channel) {
            case "MC|Brand" -> ClientboundCustomPayloadPacket.BRAND;
            case "MC|TrList" -> TRADER_LIST;
            case "MC|BOpen" -> OPEN_BOOK;
            default -> throw new IllegalStateException("This packet should have been handled a different way");
        };
    }

    @Polymorphic(stringValue = "brand")
    @MessageVariant(minVersion = Protocols.V1_13, maxVersion = Protocols.V1_13_2)
    public static class Brand extends SPacketCustomPayload_1_13_2 implements SPacketCustomPayload.Brand {
        public String brand;
    }

    @Polymorphic(stringValue = "trader_list")
    @MessageVariant(minVersion = Protocols.V1_13, maxVersion = Protocols.V1_13_2)
    public static class TraderList extends SPacketCustomPayload_1_13_2 implements SPacketCustomPayload.TraderList {
        public int syncId;
        @Length(type = Types.UNSIGNED_BYTE)
        public List<SPacketMerchantOffers.Trade> trades;

        @Handler
        public static SPacketMerchantOffers_1_14_2 handle(
                @Argument("syncId") int syncId,
                @Argument("trades") List<SPacketMerchantOffers.Trade> trades
        ) {
            var packet = new SPacketMerchantOffers_1_14_2();
            packet.syncId = syncId;
            packet.trades = trades;
            packet.villagerLevel = 5;
            packet.experience = 0;
            packet.isRegularVillager = false;
            return packet;
        }
    }

    @Polymorphic(stringValue = "open_book")
    @MessageVariant(minVersion = Protocols.V1_13, maxVersion = Protocols.V1_13_2)
    public static class OpenBook extends SPacketCustomPayload_1_13_2 implements SPacketCustomPayload.OpenBook {
        public CommonTypes.Hand hand;

        @Handler
        public static SPacketOpenBook handle(@Argument("hand") CommonTypes.Hand hand) {
            var packet = new SPacketOpenBook();
            packet.hand = hand;
            return packet;
        }
    }

    @Polymorphic(otherwise = true)
    @MessageVariant(minVersion = Protocols.V1_13, maxVersion = Protocols.V1_13_2)
    public static class Other extends SPacketCustomPayload_1_13_2 implements SPacketCustomPayload.Other {
        @Length(remainingBytes = true)
        public byte[] data;
    }
}
