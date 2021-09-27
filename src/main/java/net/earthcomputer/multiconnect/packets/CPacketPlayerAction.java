package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.NetworkEnum;

@Message
public class CPacketPlayerAction {
    public Action action;
    public CommonTypes.BlockPos pos;
    public CommonTypes.Direction face;

    @NetworkEnum
    public enum Action {
        START_DESTROY_BLOCK,
        ABORT_DESTROY_BLOCK,
        STOP_DESTROY_BLOCK,
        DROP_ALL_ITEMS,
        DROP_ITEM,
        RELEASE_USE_ITEM,
        SWAP_ITEM_WITH_OFFHAND,
    }
}
