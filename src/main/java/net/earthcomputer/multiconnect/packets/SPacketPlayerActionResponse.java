package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;

@Message
public class SPacketPlayerActionResponse {
    public CommonTypes.BlockPos pos;
    @Registry(Registries.BLOCK_STATE)
    public int block;
    public Status status;
    public boolean successful;

    public enum Status {
        STARTED_DIGGING, CANCELED_DIGGING, FINISHED_DIGGING
    }
}
