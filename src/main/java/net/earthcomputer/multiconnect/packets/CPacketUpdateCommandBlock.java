package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.NetworkEnum;

@Message
public class CPacketUpdateCommandBlock {
    public CommonTypes.BlockPos pos;
    public String command;
    public Mode mode;
    public byte flags;

    @NetworkEnum
    public enum Mode {
        SEQUENCE, AUTO, REDSTONE
    }
}
