package net.earthcomputer.multiconnect.packets.v1_16_5;

import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketPlayerSpawnPosition;

@Message(variantOf = SPacketPlayerSpawnPosition.class, maxVersion = Protocols.V1_16_5)
public class SPacketPlayerSpawnPosition_1_16_5 {
    public CommonTypes.BlockPos pos;
}
