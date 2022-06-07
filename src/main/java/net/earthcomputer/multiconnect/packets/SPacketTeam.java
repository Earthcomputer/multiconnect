package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;

@Message
public interface SPacketTeam {
    @Message
    interface Action {
    }

    @Message
    interface CreateAction {
    }

    @Message
    interface RemoveAction {
    }

    @Message
    interface UpdateInfoAction {
    }

    @Message
    interface EntitiesAction {
    }
}
