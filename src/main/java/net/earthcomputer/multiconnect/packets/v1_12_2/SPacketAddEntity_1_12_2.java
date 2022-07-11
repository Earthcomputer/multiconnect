package net.earthcomputer.multiconnect.packets.v1_12_2;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketAddEntity;

import java.util.UUID;

@MessageVariant(maxVersion = Protocols.V1_12_2)
public class SPacketAddEntity_1_12_2 implements SPacketAddEntity {
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
