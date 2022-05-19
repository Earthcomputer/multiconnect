package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketGameMessage;
import net.minecraft.util.Util;

import java.util.UUID;

@MessageVariant(minVersion = Protocols.V1_16)
public class SPacketGameMessage_Latest implements SPacketGameMessage {
    public CommonTypes.Text text;
    public byte position;
    @Introduce(compute = "computeSender")
    public UUID sender;

    public static UUID computeSender() {
        return Util.NIL_UUID;
    }
}
