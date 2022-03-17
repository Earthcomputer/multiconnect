package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Registry;
import net.earthcomputer.multiconnect.ap.Registries;

import java.util.UUID;

@MessageVariant
public class SPacketMobSpawn {
    public int entityId;
    public UUID uuid;
    @Registry(Registries.ENTITY_TYPE)
    public int type;
    public double x;
    public double y;
    public double z;
    public byte yaw;
    public byte pitch;
    public byte headYaw;
    public short velocityX;
    public short velocityY;
    public short velocityZ;
}
