package net.earthcomputer.multiconnect.packets.v1_15_2;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketSystemChat;

@MessageVariant(maxVersion = Protocols.V1_15_2)
public class SPacketSystemChat_1_15_2 implements SPacketSystemChat {
    public CommonTypes.Text text;
    public byte position;
}
