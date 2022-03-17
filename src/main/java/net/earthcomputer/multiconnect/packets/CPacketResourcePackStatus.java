package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.NetworkEnum;

@MessageVariant
public class CPacketResourcePackStatus {
    public Result result;

    @NetworkEnum
    public enum Result {
        LOADED, DECLINED, FAILED, ACCEPTED
    }
}
