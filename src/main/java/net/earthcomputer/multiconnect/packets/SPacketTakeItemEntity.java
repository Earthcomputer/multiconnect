package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;

@MessageVariant
public class SPacketTakeItemEntity {
    public int collectedEntityId;
    public int collectorEntityId;
    public int pickupItemCount;
}
