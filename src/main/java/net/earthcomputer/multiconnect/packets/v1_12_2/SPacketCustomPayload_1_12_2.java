package net.earthcomputer.multiconnect.packets.v1_12_2;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.FilledArgument;
import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.Length;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketCustomPayload;
import net.earthcomputer.multiconnect.packets.SPacketMerchantOffers;
import net.earthcomputer.multiconnect.packets.v1_13_2.SPacketCustomPayload_1_13_2;
import net.earthcomputer.multiconnect.protocols.generic.CustomPayloadHandler;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

@Polymorphic
@MessageVariant(maxVersion = Protocols.V1_12_2)
public abstract class SPacketCustomPayload_1_12_2 implements SPacketCustomPayload {
    public String channel;

    @Polymorphic(stringValue = "MC|Brand")
    @MessageVariant(maxVersion = Protocols.V1_12_2)
    public static class Brand extends SPacketCustomPayload_1_12_2 implements SPacketCustomPayload.Brand {
        public String brand;
    }

    @Polymorphic(stringValue = "MC|TrList")
    @MessageVariant(maxVersion = Protocols.V1_12_2)
    public static class TraderList extends SPacketCustomPayload_1_12_2 implements SPacketCustomPayload.TraderList {
        @Type(Types.INT)
        public int syncId;
        @Length(type = Types.UNSIGNED_BYTE)
        public List<SPacketMerchantOffers.Trade> trades;
    }

    @Polymorphic(stringValue = "MC|BOpen")
    @MessageVariant(maxVersion = Protocols.V1_12_2)
    public static class OpenBook extends SPacketCustomPayload_1_12_2 implements SPacketCustomPayload.OpenBook {
        public CommonTypes.Hand hand;
    }

    @Polymorphic(otherwise = true)
    @MessageVariant(maxVersion = Protocols.V1_12_2)
    public static class Other extends SPacketCustomPayload_1_12_2 implements SPacketCustomPayload.Other {
        @Length(remainingBytes = true)
        public byte[] data;

        @SuppressWarnings("deprecation")
        @Handler
        public static List<SPacketCustomPayload_1_13_2> handle(
                @Argument("channel") String channel,
                @Argument("data") byte[] data,
                @FilledArgument ClientPacketListener networkHandler
        ) {
            // call deprecated method
            CustomPayloadHandler.handleClientboundStringCustomPayload(networkHandler, channel, data);

            ResourceLocation newChannel = CustomPayloadHandler.getClientboundChannel112(channel);
            if (newChannel != null && CustomPayloadHandler.allowClientboundCustomPayload(newChannel)) {
                List<SPacketCustomPayload_1_13_2> packets = new ArrayList<>(1);
                var packet = new SPacketCustomPayload_1_13_2.Other();
                packet.channel = newChannel;
                packet.data = data;
                packets.add(packet);
                return packets;
            }

            return new ArrayList<>(0);
        }
    }
}
