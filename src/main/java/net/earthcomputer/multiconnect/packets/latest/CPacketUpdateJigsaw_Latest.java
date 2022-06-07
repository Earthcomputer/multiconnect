package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CPacketUpdateJigsaw;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.minecraft.util.Identifier;

@MessageVariant(minVersion = Protocols.V1_16)
public class CPacketUpdateJigsaw_Latest implements CPacketUpdateJigsaw {
    public CommonTypes.BlockPos pos;
    public Identifier name;
    public Identifier target;
    public Identifier pool;
    public String finalState;
    public String jointType;
}
