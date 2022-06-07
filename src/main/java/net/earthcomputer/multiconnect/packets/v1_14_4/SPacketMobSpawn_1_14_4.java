package net.earthcomputer.multiconnect.packets.v1_14_4;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;
import net.earthcomputer.multiconnect.ap.ReturnType;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketEntityTrackerUpdate;
import net.earthcomputer.multiconnect.packets.SPacketMobSpawn;
import net.earthcomputer.multiconnect.packets.v1_18_2.SPacketMobSpawn_1_18_2;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@MessageVariant(maxVersion = Protocols.V1_14_4)
public class SPacketMobSpawn_1_14_4 implements SPacketMobSpawn {
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
    public CommonTypes.EntityTrackerEntry dataTrackerEntries;

    @ReturnType(SPacketMobSpawn.class)
    @ReturnType(SPacketEntityTrackerUpdate.class)
    @Handler
    public static List<Object> handle(
            @Argument("entityId") int entityId,
            @Argument("uuid") UUID uuid,
            @Argument("type") int type,
            @Argument("x") double x,
            @Argument("y") double y,
            @Argument("z") double z,
            @Argument("yaw") byte yaw,
            @Argument("pitch") byte pitch,
            @Argument("headYaw") byte headYaw,
            @Argument("velocityX") short velocityX,
            @Argument("velocityY") short velocityY,
            @Argument("velocityZ") short velocityZ,
            @Argument("dataTrackerEntries") CommonTypes.EntityTrackerEntry dataTrackerEntries
    ) {
        List<Object> packets = new ArrayList<>(2);

        {
            SPacketMobSpawn_1_18_2 packet = new SPacketMobSpawn_1_18_2();
            packet.entityId = entityId;
            packet.uuid = uuid;
            packet.type = type;
            packet.x = x;
            packet.y = y;
            packet.z = z;
            packet.yaw = yaw;
            packet.pitch = pitch;
            packet.headYaw = headYaw;
            packet.velocityX = velocityX;
            packet.velocityY = velocityY;
            packet.velocityZ = velocityZ;
            packets.add(packet);
        }

        {
            SPacketEntityTrackerUpdate packet = new SPacketEntityTrackerUpdate();
            packet.entityId = entityId;
            packet.entries = dataTrackerEntries;
            packets.add(packet);
        }

        return packets;
    }
}
