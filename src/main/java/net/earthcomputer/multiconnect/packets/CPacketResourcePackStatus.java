package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;

@Message
public class CPacketResourcePackStatus {
    public Result result;

    public enum Result {
        LOADED, DECLINED, FAILED, ACCEPTED
    }
}
