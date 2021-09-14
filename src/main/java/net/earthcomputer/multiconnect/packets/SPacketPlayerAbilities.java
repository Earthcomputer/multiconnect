package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;

@Message
public class SPacketPlayerAbilities {
    public byte flags;
    public float flyingSpeed;
    public float fovModifier;
}
