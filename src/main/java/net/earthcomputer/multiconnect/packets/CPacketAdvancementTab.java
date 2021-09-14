package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.minecraft.util.Identifier;

@Message
@Polymorphic
public abstract class CPacketAdvancementTab {
    public Action action;

    public enum Action {
        OPENED, CLOSED
    }

    @Polymorphic(stringValue = "OPENED")
    @Message
    public static class Opened extends CPacketAdvancementTab {
        public Identifier tabId;
    }

    @Polymorphic(stringValue = "CLOSED")
    @Message
    public static class Closed extends CPacketAdvancementTab {
    }
}
