package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;

@MessageVariant
public class SPacketSetDisplayObjective {
    public byte position;
    public String scoreName;
}
