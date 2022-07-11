package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;

@Message
public interface SPacketBossEvent {
    @Message
    interface Action {
    }

    @Message
    interface AddAction {
    }

    @Message
    interface RemoveAction {
    }

    @Message
    interface UpdateHealthAction {
    }

    @Message
    interface UpdateTitleAction {
    }

    @Message
    interface UpdateStyleAction {
    }

    @Message
    interface UpdateFlagsAction {
    }
}
