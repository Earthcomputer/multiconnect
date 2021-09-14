package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;

@Message
public class CPacketUpdateCommandBlock {
    public CommonTypes.BlockPos pos;
    public String command;
    public Mode mode;
    public byte flags;

    public enum Mode {
        SEQUENCE, AUTO, REDSTONE
    }
}
