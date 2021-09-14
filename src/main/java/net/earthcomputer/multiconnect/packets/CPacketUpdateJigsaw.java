package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;
import net.minecraft.util.Identifier;

@Message
public class CPacketUpdateJigsaw {
    public CommonTypes.BlockPos pos;
    public Identifier name;
    public Identifier target;
    public Identifier pool;
    public String finalState;
    public String jointType;
}
