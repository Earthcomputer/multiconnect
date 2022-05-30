package net.earthcomputer.multiconnect.packets.v1_13_2;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketEntitySpawn;
import net.earthcomputer.multiconnect.protocols.v1_12_2.Blocks_1_12_2;

import java.util.UUID;

@MessageVariant(minVersion = Protocols.V1_13, maxVersion = Protocols.V1_13_2)
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
    @Introduce(compute = "computeData")
    public int data;
    public short velocityX;
    public short velocityY;
    public short velocityZ;

    public static int computeData(
            @Argument("type") byte type,
            @Argument("data") int data
    ) {
        if (type == 70) { // falling block
            return Blocks_1_12_2.convertToStateRegistryId(data);
        } else if (type == 71) { // item frame
            return switch (data) {
                case 0 -> 3; // south
                case 1 -> 4; // west
                case 2 -> 2; // north
                case 3 -> 5; // east
                default -> 2;
            };
        } else {
            return data;
        }
    }
}
