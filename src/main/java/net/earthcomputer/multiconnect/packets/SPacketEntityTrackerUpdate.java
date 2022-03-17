package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;

@MessageVariant
public class SPacketEntityTrackerUpdate {
    public int entityId;
    public CommonTypes.EntityTrackerEntry entries;
}
