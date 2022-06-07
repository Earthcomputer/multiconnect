package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketGameMessage;

@MessageVariant(minVersion = Protocols.V1_19)
public class SPacketGameMessage_Latest implements SPacketGameMessage {
    public CommonTypes.Text text;
    public int messageType;
}
