package net.earthcomputer.multiconnect.packets.v1_18_2;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CPacketChatMessage;

@MessageVariant(maxVersion = Protocols.V1_18_2)
public class CPacketChatMessage_1_18_2 implements CPacketChatMessage {
    public String message;
}
