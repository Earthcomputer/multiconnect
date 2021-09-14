package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;

@Message
public class SPacketWorldTimeUpdate {
    @Type(Types.LONG)
    public long gameTime;
    @Type(Types.LONG)
    public long timeOfDay;
}
