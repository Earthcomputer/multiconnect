package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;

@MessageVariant
public class SPacketSetEntityData {
    public int entityId;
    public CommonTypes.EntityTrackerEntry entries;
}
