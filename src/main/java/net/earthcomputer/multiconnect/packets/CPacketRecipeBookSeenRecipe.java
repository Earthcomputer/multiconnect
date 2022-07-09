package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;

@Message
public interface CPacketRecipeBookSeenRecipe {
    @Message
    interface Shown {
    }

    @Message
    interface Settings {
    }
}
