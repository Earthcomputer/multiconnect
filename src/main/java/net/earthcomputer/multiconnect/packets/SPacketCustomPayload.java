package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Length;
import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.minecraft.util.Identifier;

@Message
@Polymorphic
public abstract class SPacketCustomPayload {
    public Identifier channel;

    @Polymorphic(stringValue = "brand")
    @Message
    public static class BrandPayload extends SPacketCustomPayload {
        public String brand;
    }

    @Polymorphic(otherwise = true)
    @Message
    public static class OtherPayload extends SPacketCustomPayload {
        @Length(remainingBytes = true)
        public byte[] payload;
    }
}
