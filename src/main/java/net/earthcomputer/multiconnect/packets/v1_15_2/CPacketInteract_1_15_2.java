package net.earthcomputer.multiconnect.packets.v1_15_2;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CPacketInteract;
import net.earthcomputer.multiconnect.packets.latest.CPacketInteract_Latest;

@MessageVariant(maxVersion = Protocols.V1_15_2)
public class CPacketInteract_1_15_2 implements CPacketInteract {
    public int entityId;
    public CPacketInteract_Latest.Action action;
}
