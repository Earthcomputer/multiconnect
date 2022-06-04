package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketEntitySpawn;

import java.util.UUID;

@MessageVariant(minVersion = Protocols.V1_19)
public class SPacketEntitySpawn_Latest implements SPacketEntitySpawn {
    public int entityId;
    public UUID uuid;
    @Registry(Registries.ENTITY_TYPE)
    public int type;
    public double x;
    public double y;
    public double z;
    public byte pitch;
    public byte yaw;
    @Introduce(compute = "computeHeadYaw")
    public byte headYaw;
    public int data;
    public short velocityX;
    public short velocityY;
    public short velocityZ;

    public static byte computeHeadYaw(@Argument("yaw") byte yaw) {
        return yaw;
    }
}
