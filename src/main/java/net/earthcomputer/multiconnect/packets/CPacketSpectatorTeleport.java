package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;

import java.util.UUID;

@MessageVariant
public class CPacketSpectatorTeleport {
    public UUID targetPlayer;
}
