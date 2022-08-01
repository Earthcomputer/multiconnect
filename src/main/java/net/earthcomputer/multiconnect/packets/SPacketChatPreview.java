package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;

import java.util.Optional;

@MessageVariant
public class SPacketChatPreview {
    @Type(Types.INT)
    public int queryId;
    public Optional<CommonTypes.Text> message;
}
