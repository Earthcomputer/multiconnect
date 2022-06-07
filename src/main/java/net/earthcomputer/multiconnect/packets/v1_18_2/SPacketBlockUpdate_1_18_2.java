package net.earthcomputer.multiconnect.packets.v1_18_2;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketBlockUpdate;

@MessageVariant(maxVersion = Protocols.V1_18_2)
public class SPacketBlockUpdate_1_18_2 implements SPacketBlockUpdate {
    public CommonTypes.BlockPos pos;
    @Registry(Registries.BLOCK_STATE)
    public int stateId;
}
