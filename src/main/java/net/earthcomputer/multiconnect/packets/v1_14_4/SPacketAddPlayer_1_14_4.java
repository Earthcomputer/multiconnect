package net.earthcomputer.multiconnect.packets.v1_14_4;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.ReturnType;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketAddPlayer;
import net.earthcomputer.multiconnect.packets.SPacketSetEntityData;
import net.earthcomputer.multiconnect.packets.latest.SPacketAddPlayer_Latest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@MessageVariant(maxVersion = Protocols.V1_14_4)
public class SPacketAddPlayer_1_14_4 implements SPacketAddPlayer {
    public int entityId;
    public UUID uuid;
    public double x;
    public double y;
    public double z;
    public byte yaw;
    public byte pitch;
    public CommonTypes.EntityTrackerEntry dataTrackerEntries;

    @ReturnType(SPacketAddPlayer.class)
    @ReturnType(SPacketSetEntityData.class)
    @Handler
    public static List<Object> handle(
            @Argument("entityId") int entityId,
            @Argument("uuid") UUID uuid,
            @Argument("x") double x,
            @Argument("y") double y,
            @Argument("z") double z,
            @Argument("yaw") byte yaw,
            @Argument("pitch") byte pitch,
            @Argument("dataTrackerEntries") CommonTypes.EntityTrackerEntry dataTrackerEntries
    ) {
        List<Object> packets = new ArrayList<>(2);

        {
            SPacketAddPlayer_Latest packet = new SPacketAddPlayer_Latest();
            packet.entityId = entityId;
            packet.uuid = uuid;
            packet.x = x;
            packet.y = y;
            packet.z = z;
            packet.yaw = yaw;
            packet.pitch = pitch;
            packets.add(packet);
        }

        {
            SPacketSetEntityData packet = new SPacketSetEntityData();
            packet.entityId = entityId;
            packet.entries = dataTrackerEntries;
            packets.add(packet);
        }

        return packets;
    }
}
