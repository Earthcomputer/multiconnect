package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.v1_12_2.CPacketCustomPayload_1_12_2;
import net.earthcomputer.multiconnect.packets.v1_12_2.CPacketCustomPayload_1_12_2.AdvCmd;

@MessageVariant(minVersion = Protocols.V1_13)
public class CPacketUpdateCommandBlockMinecart {
    public int entityId;
    public String command;
    public boolean trackOutput;

    @Handler(protocol = Protocols.V1_12_2)
    public static CPacketCustomPayload_1_12_2 toCustomPayload(
            @Argument("entityId") int entityId,
            @Argument("command") String command,
            @Argument("trackOutput") boolean trackOutput
    ) {
        var packet = new CPacketCustomPayload_1_12_2.AdvCmd();
        packet.channel = "MC|AdvCmd";
        packet.type = 1; // command block type (minecart)
        packet.entityId = entityId;
        packet.command = command;
        packet.trackOutput = trackOutput;
        return packet;
    }
}
