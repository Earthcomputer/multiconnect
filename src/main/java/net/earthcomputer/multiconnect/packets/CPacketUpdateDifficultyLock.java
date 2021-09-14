package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;

@Message
public class CPacketUpdateDifficultyLock {
    public boolean locked;
}
