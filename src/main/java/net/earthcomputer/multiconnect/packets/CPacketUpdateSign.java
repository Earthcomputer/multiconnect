package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Length;
import net.earthcomputer.multiconnect.ap.MessageVariant;

import java.util.List;

@MessageVariant
public class CPacketUpdateSign {
    public CommonTypes.BlockPos pos;
    @Length(constant = 4)
    public List<String> lines;
}
