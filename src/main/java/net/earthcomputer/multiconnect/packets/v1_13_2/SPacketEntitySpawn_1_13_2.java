package net.earthcomputer.multiconnect.packets.v1_13_2;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketEntitySpawn;

import java.util.UUID;

@MessageVariant(maxVersion = Protocols.V1_13_2)
public class SPacketEntitySpawn_1_13_2 implements SPacketEntitySpawn {
    public int entityId;
    public UUID uuid;
    public byte type;
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
