package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.NetworkEnum;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;

@MessageVariant
public class SPacketPlayerActionResponse {
    public CommonTypes.BlockPos pos;
    @Registry(Registries.BLOCK_STATE)
    public int block;
    public Status status;
    public boolean successful;

    @NetworkEnum
    public enum Status {
        STARTED_DIGGING, CANCELED_DIGGING, FINISHED_DIGGING
    }
}
