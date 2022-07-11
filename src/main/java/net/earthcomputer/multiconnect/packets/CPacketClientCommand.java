package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.NetworkEnum;

@MessageVariant
public class CPacketClientCommand {
    public Action action;

    @NetworkEnum
    public enum Action {
        RESPAWN, REQUEST_STATS
    }
}
