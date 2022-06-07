package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;

@MessageVariant
public class SPacketWorldBorderInterpolateSize {
    public double oldDiameter;
    public double newDiameter;
    public long time;
}
