package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;

@Message
public class CPacketClientStatus {
    public Action action;

    public enum Action {
        RESPAWN, REQUEST_STATS
    }
}
