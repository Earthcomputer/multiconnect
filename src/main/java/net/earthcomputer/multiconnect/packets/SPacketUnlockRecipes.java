package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;

@Message
public interface SPacketUnlockRecipes {
    @Message
    interface Init {
    }

    @Message
    interface Other {
    }
}
