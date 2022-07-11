package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;

@Message
public interface SPacketRecipe {
    @Message
    interface Init {
    }

    @Message
    interface Other {
    }
}
