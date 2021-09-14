package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;

@Message
public class SPacketExperienceOrbSpawn {
    public int entityId;
    public double x;
    public double y;
    public double z;
    public short count;
}
