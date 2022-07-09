package net.earthcomputer.multiconnect.packets.v1_15_2;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.DefaultConstruct;
import net.earthcomputer.multiconnect.ap.FilledArgument;
import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.v1_18_2.SPacketEntitySpawn_1_18_2;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import java.util.ArrayList;
import java.util.List;

@MessageVariant(maxVersion = Protocols.V1_15_2)
public class SPacketEntitySpawnGlobal_1_15_2 {
    private static final RandomSource random = RandomSource.createNewThreadLocalInstance();

    public int id;
    public byte entityTypeId;
    public double x;
    public double y;
    public double z;

    @Handler
    public static List<SPacketEntitySpawn_1_18_2> toEntitySpawnPacket(
            @Argument("entityTypeId") byte entityTypeId,
            @Argument("x") double x,
            @Argument("y") double y,
            @Argument("z") double z,
            @DefaultConstruct SPacketEntitySpawn_1_18_2 packet,
            @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.ENTITY_TYPE, value = "lightning_bolt")) int lightningBoltId
    ) {
        if (entityTypeId != 1) { // lightning bolt id
            return new ArrayList<>(0);
        }

        packet.entityId = random.nextInt(); // let's hope this doesn't collide (not important, only a lightning bolt)
        packet.type = lightningBoltId;
        packet.uuid = Mth.createInsecureUUID(random);
        packet.x = x;
        packet.y = y;
        packet.z = z;
        List<SPacketEntitySpawn_1_18_2> packets = new ArrayList<>(1);
        packets.add(packet);
        return packets;
    }
}
