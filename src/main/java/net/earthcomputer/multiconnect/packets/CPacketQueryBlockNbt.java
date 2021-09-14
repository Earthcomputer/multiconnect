package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;

@Message
public class CPacketQueryBlockNbt {
    public int transactionId;
    public CommonTypes.BlockPos pos;
}
