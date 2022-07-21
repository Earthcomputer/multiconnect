package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketAddPlayer;

import java.util.UUID;

@MessageVariant(minVersion = Protocols.V1_15)
public class SPacketAddPlayer_Latest implements SPacketAddPlayer {
    public int entityId;
    public UUID uuid;
    public double x;
    public double y;
    public double z;
    public byte yaw;
    public byte pitch;
}
