package net.earthcomputer.multiconnect.packets.v1_16_5;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketSetDefaultSpawnPosition;

@MessageVariant(maxVersion = Protocols.V1_16_5)
public class SPacketSetDefaultSpawnPosition_1_16_5 implements SPacketSetDefaultSpawnPosition {
    public CommonTypes.BlockPos pos;
}
