package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Registry;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketMobSpawn;

import java.util.UUID;

@MessageVariant(minVersion = Protocols.V1_15)
public class SPacketMobSpawn_Latest implements SPacketMobSpawn {
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
