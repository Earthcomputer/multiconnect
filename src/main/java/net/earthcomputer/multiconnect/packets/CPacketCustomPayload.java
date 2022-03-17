package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Length;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.minecraft.util.Identifier;

@MessageVariant
@Polymorphic
public abstract class CPacketCustomPayload {
    public Identifier channel;

    @Polymorphic(stringValue = "brand")
    @MessageVariant
    public static class BrandPayload extends CPacketCustomPayload {
        public String brand;
    }

    @Polymorphic(otherwise = true)
    @MessageVariant
    public static class OtherPayload extends CPacketCustomPayload {
        @Length(remainingBytes = true)
        public byte[] data;
    }
}
