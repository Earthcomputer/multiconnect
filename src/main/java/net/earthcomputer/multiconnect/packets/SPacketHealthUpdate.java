package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;

@MessageVariant
public class SPacketHealthUpdate {
    public float health;
    public int food;
    public float saturation;
}
