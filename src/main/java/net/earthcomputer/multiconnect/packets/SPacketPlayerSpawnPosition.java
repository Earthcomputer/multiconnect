package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.api.Protocols;

@Message(minVersion = Protocols.V1_17)
public class SPacketPlayerSpawnPosition {
    public CommonTypes.BlockPos pos;
    @Introduce(doubleValue = 0)
    public float angle;
}
