package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketPlayerPositionLook;

@MessageVariant(minVersion = Protocols.V1_17)
public class SPacketPlayerPositionLook_Latest implements SPacketPlayerPositionLook {
    public double x;
    public double y;
    public double z;
    public float yaw;
    public float pitch;
    public byte flags;
    public int teleportId;
    @Introduce(booleanValue = false)
    public boolean dismountVehicle;
}
