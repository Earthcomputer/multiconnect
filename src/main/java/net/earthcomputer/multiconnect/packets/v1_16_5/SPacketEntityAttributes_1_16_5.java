package net.earthcomputer.multiconnect.packets.v1_16_5;

import net.earthcomputer.multiconnect.ap.Length;
import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.packets.SPacketEntityAttributes;

import java.util.List;

@Message
public class SPacketEntityAttributes_1_16_5 {
    public int entityId;
    @Length(type = Types.INT)
    public List<SPacketEntityAttributes.Property> properties;
}
