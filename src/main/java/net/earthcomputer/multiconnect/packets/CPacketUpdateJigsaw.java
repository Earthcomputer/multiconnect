package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.minecraft.util.Identifier;

@MessageVariant
public class CPacketUpdateJigsaw {
    public CommonTypes.BlockPos pos;
    public Identifier name;
    public Identifier target;
    public Identifier pool;
    public String finalState;
    public String jointType;
}
