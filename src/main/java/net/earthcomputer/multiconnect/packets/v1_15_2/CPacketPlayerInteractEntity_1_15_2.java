package net.earthcomputer.multiconnect.packets.v1_15_2;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CPacketPlayerInteractEntity;
import net.earthcomputer.multiconnect.packets.latest.CPacketPlayerInteractEntity_Latest;

@MessageVariant(maxVersion = Protocols.V1_15_2)
public class CPacketPlayerInteractEntity_1_15_2 implements CPacketPlayerInteractEntity {
    public int entityId;
    public CPacketPlayerInteractEntity_Latest.Action action;
}
