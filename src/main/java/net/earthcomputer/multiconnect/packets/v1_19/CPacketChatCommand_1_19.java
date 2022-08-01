package net.earthcomputer.multiconnect.packets.v1_19;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CPacketChatCommand;
import net.earthcomputer.multiconnect.packets.latest.CPacketChatCommand_Latest;
import net.earthcomputer.multiconnect.packets.v1_18_2.CPacketChat_1_18_2;

import java.util.List;

@MessageVariant(minVersion = Protocols.V1_19, maxVersion = Protocols.V1_19)
public class CPacketChatCommand_1_19 implements CPacketChatCommand {
    public String command;
    @Type(Types.LONG)
    public long timestamp;
    @Type(Types.LONG)
    public long salt;
    public List<CPacketChatCommand_Latest.ArgumentSignature> argumentSignatures;
    public boolean previewChat;

    @Handler(protocol = Protocols.V1_18_2)
    public static CPacketChat_1_18_2 handle(@Argument("command") String command) {
        var packet = new CPacketChat_1_18_2();
        packet.message = "/" + command;
        return packet;
    }
}
