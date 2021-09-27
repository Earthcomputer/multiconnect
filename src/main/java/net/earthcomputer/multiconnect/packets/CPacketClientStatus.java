package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.NetworkEnum;

@Message
public class CPacketClientStatus {
    public Action action;

    @NetworkEnum
    public enum Action {
        RESPAWN, REQUEST_STATS
    }
}
