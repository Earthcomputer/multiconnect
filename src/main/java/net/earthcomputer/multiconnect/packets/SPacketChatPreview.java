package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;

import java.util.Optional;

@MessageVariant
public class SPacketChatPreview {
    public int queryId;
    public Optional<CommonTypes.Text> message;
}
