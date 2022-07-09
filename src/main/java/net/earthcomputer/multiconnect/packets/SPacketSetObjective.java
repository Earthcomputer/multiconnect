package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;

@Message
public interface SPacketSetObjective {
    @Message
    interface Action {
    }

    @Message
    interface AdditiveAction {
    }

    @Message
    interface RemoveAction {
    }
}
