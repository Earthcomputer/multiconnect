package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;

import java.util.Optional;
import java.util.UUID;

@MessageVariant
public class SPacketChatMessage {
    public CommonTypes.Text text;
    public int messageType;
    public UUID sender;
    public CommonTypes.Text displayName;
    public Optional<CommonTypes.Text> teamDisplayName;
    @Type(Types.LONG)
    public long timestamp;
    @Type(Types.LONG)
    public long salt;
    public byte[] signature;
}
