package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;

@Message
public class SPacketItemPickupAnimation {
    public int collectedEntityId;
    public int collectorEntityId;
    public int pickupItemCount;
}
