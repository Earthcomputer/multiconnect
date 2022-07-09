package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;

@MessageVariant
public class SPacketBlockDestruction {
    public int entityId;
    public CommonTypes.BlockPos pos;
    public byte destroyStage;
}
