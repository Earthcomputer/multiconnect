package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.Length;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketCustomPayload;
import net.minecraft.util.Identifier;

@MessageVariant(minVersion = Protocols.V1_14)
@Polymorphic
public abstract class SPacketCustomPayload_Latest implements SPacketCustomPayload {
    public Identifier channel;

    @Polymorphic(stringValue = "brand")
    @MessageVariant(minVersion = Protocols.V1_14)
    public static class BrandPayload extends SPacketCustomPayload_Latest implements Brand {
        public String brand;
    }

    @Polymorphic(otherwise = true)
    @MessageVariant(minVersion = Protocols.V1_14)
    public static class OtherPayload extends SPacketCustomPayload_Latest implements Other {
        @Length(remainingBytes = true)
        public byte[] payload;
    }
}
