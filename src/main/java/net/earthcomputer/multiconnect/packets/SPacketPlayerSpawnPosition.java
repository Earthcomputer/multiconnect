package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.Protocol;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.v1_16_5.SPacketPlayerSpawnPosition_1_16_5;

@Message(translateFromOlder = @Protocol(value = Protocols.V1_16_5, type = SPacketPlayerSpawnPosition_1_16_5.class))
public class SPacketPlayerSpawnPosition {
    public CommonTypes.BlockPos pos;
    @Introduce(doubleValue = 0)
    public float angle;
}
