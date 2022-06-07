package net.earthcomputer.multiconnect.packets.v1_15_2;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketGameMessage;

@MessageVariant(maxVersion = Protocols.V1_15_2)
public class SPacketGameMessage_1_15_2 implements SPacketGameMessage {
    public CommonTypes.Text text;
    public byte position;
}
