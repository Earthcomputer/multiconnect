package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;

import java.util.UUID;

@MessageVariant
public class CPacketTeleportToEntity {
    public UUID targetPlayer;
}
