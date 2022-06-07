package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;

@Message
public interface CPacketRecipeBookData {
    @Message
    interface Shown {
    }

    @Message
    interface Settings {
    }
}
