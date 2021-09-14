package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.Registry;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;

import java.util.UUID;

@Message
public class SPacketEntitySpawn {
    public int entityId;
    public UUID uuid;
    @Registry(Registries.ENTITY_TYPE)
    public int type;
    public double x;
    public double y;
    public double z;
    public byte pitch;
    public byte yaw;
    @Type(Types.INT)
    public int data;
    public short velocityX;
    public short velocityY;
    public short velocityZ;
}
