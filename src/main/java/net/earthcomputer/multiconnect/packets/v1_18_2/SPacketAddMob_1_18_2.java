package net.earthcomputer.multiconnect.packets.v1_18_2;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Registry;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketAddMob;
import net.earthcomputer.multiconnect.packets.latest.SPacketAddEntity_Latest;

import java.util.UUID;

@MessageVariant(minVersion = Protocols.V1_15, maxVersion = Protocols.V1_18_2)
public class SPacketAddMob_1_18_2 implements SPacketAddMob {
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

    @Handler
    public static SPacketAddEntity_Latest handle(@Argument("this") SPacketAddMob_1_18_2 self) {
        var packet = new SPacketAddEntity_Latest();
        packet.entityId = self.entityId;
        packet.uuid = self.uuid;
        packet.type = self.type;
        packet.x = self.x;
        packet.y = self.y;
        packet.z = self.z;
        packet.yaw = self.yaw;
        packet.pitch = self.pitch;
        packet.headYaw = self.headYaw;
        packet.velocityX = self.velocityX;
        packet.velocityY = self.velocityY;
        packet.velocityZ = self.velocityZ;
        return packet;
    }
}
