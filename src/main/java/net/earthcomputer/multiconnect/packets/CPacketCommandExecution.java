package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.v1_18_2.CPacketChatMessage_1_18_2;

import java.util.List;

@MessageVariant(minVersion = Protocols.V1_19)
public class CPacketCommandExecution {
    public String command;
    @Type(Types.LONG)
    public long timestamp;
    @Type(Types.LONG)
    public long salt;
    public List<ArgumentSignature> argumentSignatures;
    public boolean previewChat;

    @Handler(protocol = Protocols.V1_18_2)
    public static CPacketChatMessage_1_18_2 handle(@Argument("command") String command) {
        var packet = new CPacketChatMessage_1_18_2();
        packet.message = "/" + command;
        return packet;
    }

    @MessageVariant
    public static class ArgumentSignature {
        public String argument;
        public byte[] signature;
    }
}
