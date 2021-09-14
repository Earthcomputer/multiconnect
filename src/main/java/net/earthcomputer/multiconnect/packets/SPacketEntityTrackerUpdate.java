package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;

@Message
public class SPacketEntityTrackerUpdate {
    public int entityId;
    public CommonTypes.EntityTrackerEntry entries;
}
