package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;

@MessageVariant
public class SPacketSetHealth {
    public float health;
    public int food;
    public float saturation;
}
