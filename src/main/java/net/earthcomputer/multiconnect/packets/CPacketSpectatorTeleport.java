package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;

import java.util.UUID;

@Message
public class CPacketSpectatorTeleport {
    public UUID targetPlayer;
}
