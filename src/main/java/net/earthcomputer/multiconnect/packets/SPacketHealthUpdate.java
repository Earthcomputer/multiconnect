package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;

@Message
public class SPacketHealthUpdate {
    public float health;
    public int food;
    public float saturation;
}
