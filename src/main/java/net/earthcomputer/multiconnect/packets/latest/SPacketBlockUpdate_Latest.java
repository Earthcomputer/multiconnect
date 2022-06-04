package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketBlockUpdate;

@MessageVariant(minVersion = Protocols.V1_19)
public class SPacketBlockUpdate_Latest implements SPacketBlockUpdate {
    public CommonTypes.BlockPos pos;
    @Registry(Registries.BLOCK_STATE)
    public int stateId;
}
