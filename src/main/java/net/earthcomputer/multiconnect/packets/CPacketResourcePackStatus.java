package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.NetworkEnum;

@Message
public class CPacketResourcePackStatus {
    public Result result;

    @NetworkEnum
    public enum Result {
        LOADED, DECLINED, FAILED, ACCEPTED
    }
}
