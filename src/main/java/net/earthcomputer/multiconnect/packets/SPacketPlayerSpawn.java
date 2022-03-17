package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;

import java.util.UUID;

@MessageVariant
public class SPacketPlayerSpawn {
    public int entityId;
    public UUID uuid;
    public double x;
    public double y;
    public double z;
    public byte yaw;
    public byte pitch;
}
