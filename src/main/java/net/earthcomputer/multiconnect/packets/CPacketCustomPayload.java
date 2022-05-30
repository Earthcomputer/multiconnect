package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;

@Message
public interface CPacketCustomPayload {
    @Message
    interface Brand {
    }

    @Message
    interface BookEdit {
    }

    @Message
    interface BookSign {
    }

    @Message
    interface PickItem {
    }

    @Message
    interface ItemName {
    }

    @Message
    interface TradeSelect {
    }

    @Message
    interface Beacon {
    }

    @Message
    interface AutoCmd {
    }

    @Message
    interface AdvCmd {
    }

    @Message
    interface Struct {
    }

    @Message
    interface Other {
    }
}
