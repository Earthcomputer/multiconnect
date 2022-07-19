package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.Length;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.earthcomputer.multiconnect.ap.Sendable;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CPacketCustomPayload;
import net.earthcomputer.multiconnect.packets.v1_12_2.CPacketCustomPayload_1_12_2;
import net.earthcomputer.multiconnect.protocols.generic.CustomPayloadHandler;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

@MessageVariant(minVersion = Protocols.V1_13)
@Polymorphic
@Sendable(from = {}, fromLatest = true)
public abstract class CPacketCustomPayload_Latest implements CPacketCustomPayload {
    public ResourceLocation channel;

    @Polymorphic(stringValue = "brand")
    @MessageVariant(minVersion = Protocols.V1_13)
    public static class Brand extends CPacketCustomPayload_Latest implements CPacketCustomPayload.Brand {
        public String brand;
    }

    @Polymorphic(otherwise = true)
    @MessageVariant(minVersion = Protocols.V1_13)
    public static class Other extends CPacketCustomPayload_Latest implements CPacketCustomPayload.Other {
        @Length(remainingBytes = true)
        public byte[] data;

        @Handler(protocol = Protocols.V1_12_2)
        public static List<CPacketCustomPayload_1_12_2> handle(
            @Argument("channel") ResourceLocation channel,
            @Argument("data") byte[] data
        ) {
            List<CPacketCustomPayload_1_12_2> packets = new ArrayList<>(1);
            var packet = new CPacketCustomPayload_1_12_2.Other();
            packet.channel = CustomPayloadHandler.getServerboundChannel112(channel);
            packet.data = data;
            packets.add(packet);
            return packets;
        }
    }
}
