package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Length;
import net.earthcomputer.multiconnect.ap.Message;

import java.util.List;

@Message
public class CPacketUpdateSign {
    public CommonTypes.BlockPos pos;
    @Length(constant = 4)
    public List<String> lines;
}
