package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;

@MessageVariant
public class SPacketSetTitlesAnimation {
    @Type(Types.INT)
    public int fadeIn;
    @Type(Types.INT)
    public int stay;
    @Type(Types.INT)
    public int fadeOut;
}
