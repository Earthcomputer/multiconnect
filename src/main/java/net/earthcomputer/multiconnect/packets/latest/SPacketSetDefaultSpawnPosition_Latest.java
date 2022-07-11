package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketSetDefaultSpawnPosition;

@MessageVariant(minVersion = Protocols.V1_17)
public class SPacketSetDefaultSpawnPosition_Latest implements SPacketSetDefaultSpawnPosition {
    public CommonTypes.BlockPos pos;
    @Introduce(doubleValue = 0)
    public float angle;
}
