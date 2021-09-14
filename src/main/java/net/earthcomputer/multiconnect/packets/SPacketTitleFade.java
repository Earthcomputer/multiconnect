package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;

@Message
public class SPacketTitleFade {
    @Type(Types.INT)
    public int fadeIn;
    @Type(Types.INT)
    public int stay;
    @Type(Types.INT)
    public int fadeOut;
}
