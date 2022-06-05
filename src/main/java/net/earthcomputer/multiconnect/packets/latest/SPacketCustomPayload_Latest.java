package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.FilledArgument;
import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.Length;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketCustomPayload;
import net.earthcomputer.multiconnect.protocols.generic.CustomPayloadHandler;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.util.Identifier;

@MessageVariant(minVersion = Protocols.V1_14)
@Polymorphic
public abstract class SPacketCustomPayload_Latest implements SPacketCustomPayload {
    public Identifier channel;

    @Polymorphic(stringValue = "brand")
    @MessageVariant(minVersion = Protocols.V1_14)
    public static class Brand extends SPacketCustomPayload_Latest implements SPacketCustomPayload.Brand {
        public String brand;
    }

    @Polymorphic(otherwise = true)
    @MessageVariant(minVersion = Protocols.V1_14)
    public static class Other extends SPacketCustomPayload_Latest implements SPacketCustomPayload.Other {
        @Length(remainingBytes = true)
        public byte[] data;

        @Handler
        public static void handle(
                @Argument("channel") Identifier channel,
                @Argument("data") byte[] data,
                @FilledArgument ClientPlayNetworkHandler networkHandler
        ) {
            CustomPayloadHandler.handleClientboundIdentifierCustomPayload(networkHandler, channel, data);
        }
    }
}
