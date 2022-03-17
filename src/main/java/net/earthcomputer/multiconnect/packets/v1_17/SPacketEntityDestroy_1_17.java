package net.earthcomputer.multiconnect.packets.v1_17;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.DefaultConstruct;
import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.packets.SPacketEntitiesDestroy;

@MessageVariant
public class SPacketEntityDestroy_1_17 {
    public int entityId;

    @Handler
    public static SPacketEntitiesDestroy handle(
            @Argument("entityId") int entityId,
            @DefaultConstruct SPacketEntitiesDestroy newPacket
    ) {
        newPacket.entityIds.add(entityId);
        return newPacket;
    }
}
