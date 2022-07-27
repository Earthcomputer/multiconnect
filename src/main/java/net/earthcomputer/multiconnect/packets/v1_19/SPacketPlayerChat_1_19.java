package net.earthcomputer.multiconnect.packets.v1_19;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketPlayerChat;

import java.util.Optional;
import java.util.UUID;

@MessageVariant(minVersion = Protocols.V1_19, maxVersion = Protocols.V1_19)
public class SPacketPlayerChat_1_19 implements SPacketPlayerChat {
    public CommonTypes.Text signedContent;
    public Optional<CommonTypes.Text> unsignedContent;
    public int chatType;
    public UUID sender;
    public CommonTypes.Text displayName;
    public Optional<CommonTypes.Text> teamDisplayName;
    @Type(Types.LONG)
    public long timestamp;
    @Type(Types.LONG)
    public long salt;
    public byte[] messageSignature;
}
