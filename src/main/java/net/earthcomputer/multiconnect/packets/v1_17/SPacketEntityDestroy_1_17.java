package net.earthcomputer.multiconnect.packets.v1_17;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.DefaultConstruct;
import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketRemoveEntities;

@MessageVariant(minVersion = Protocols.V1_17, maxVersion = Protocols.V1_17)
public class SPacketEntityDestroy_1_17 {
    public int entityId;

    @Handler
    public static SPacketRemoveEntities handle(
            @Argument("entityId") int entityId,
            @DefaultConstruct SPacketRemoveEntities newPacket
    ) {
        newPacket.entityIds.add(entityId);
        return newPacket;
    }
}
