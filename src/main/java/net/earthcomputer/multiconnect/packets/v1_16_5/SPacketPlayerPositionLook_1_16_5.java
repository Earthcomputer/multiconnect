package net.earthcomputer.multiconnect.packets.v1_16_5;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketPlayerPositionLook;

@MessageVariant(minVersion = Protocols.V1_14, maxVersion = Protocols.V1_16_5)
public class SPacketPlayerPositionLook_1_16_5 implements SPacketPlayerPositionLook {
    public double x;
    public double y;
    public double z;
    public float yaw;
    public float pitch;
    public byte flags;
    public int teleportId;
}
