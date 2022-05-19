package net.earthcomputer.multiconnect.packets.v1_15_2;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CPacketUpdateJigsaw;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.minecraft.util.Identifier;

@MessageVariant(maxVersion = Protocols.V1_15_2)
public class CPacketUpdateJigsaw_1_15_2 implements CPacketUpdateJigsaw {
    public CommonTypes.BlockPos pos;
    public Identifier target;
    public Identifier pool;
    public String finalState;
}
