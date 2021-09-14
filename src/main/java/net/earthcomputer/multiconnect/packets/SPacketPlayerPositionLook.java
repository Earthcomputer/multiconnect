package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.Protocol;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.v1_16_5.SPacketPlayerPositionLook_1_16_5;

@Message(translateFromOlder = @Protocol(value = Protocols.V1_16_5, type = SPacketPlayerPositionLook_1_16_5.class))
public class SPacketPlayerPositionLook {
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
