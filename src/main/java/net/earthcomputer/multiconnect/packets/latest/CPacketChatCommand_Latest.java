package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.Length;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CPacketChatCommand;

import java.util.List;

@MessageVariant(minVersion = Protocols.V1_19_1)
public class CPacketChatCommand_Latest implements CPacketChatCommand {
    public String command;
    @Type(Types.LONG)
    public long timestamp;
    @Type(Types.LONG)
    public long salt;
    public List<ArgumentSignature> argumentSignatures;
    public boolean previewChat;
    public SPacketPlayerChat_Latest.LastSeenUpdate lastSeenMessages;

    @MessageVariant
    public static class ArgumentSignature {
        @Length(max = 16)
        public String argument;
        public byte[] signature;
    }
}
