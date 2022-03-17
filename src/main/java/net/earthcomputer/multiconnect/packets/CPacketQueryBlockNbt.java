package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;

@MessageVariant
public class CPacketQueryBlockNbt {
    public int transactionId;
    public CommonTypes.BlockPos pos;
}
