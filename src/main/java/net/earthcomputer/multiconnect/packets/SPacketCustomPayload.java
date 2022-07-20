package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;

@Message
public interface SPacketCustomPayload {
    @Message
    interface Brand {
    }

    @Message
    interface TraderList {
    }

    @Message
    interface OpenBook {
    }

    @Message
    interface Other {
    }
}