package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.NetworkEnum;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.v1_12_2.CPacketCustomPayload_1_12_2;
import net.earthcomputer.multiconnect.packets.v1_12_2.CPacketCustomPayload_1_12_2.AutoCmd;
import net.minecraft.core.BlockPos;

@MessageVariant(minVersion = Protocols.V1_13)
public class CPacketUpdateCommandBlock {
    public CommonTypes.BlockPos pos;
    public String command;
    public Mode mode;
    public byte flags;

    @Handler(protocol = Protocols.V1_12_2)
    public static CPacketCustomPayload_1_12_2 toCustomPayload(
            @Argument("pos") CommonTypes.BlockPos pos,
            @Argument("command") String command,
            @Argument("mode") Mode mode,
            @Argument("flags") byte flags
    ) {
        var packet = new CPacketCustomPayload_1_12_2.AutoCmd();
        packet.channel = "MC|AutoCmd";
        var mcPos = pos.toMinecraft();
        packet.x = mcPos.getX();
        packet.y = mcPos.getY();
        packet.z = mcPos.getZ();
        packet.command = command;
        packet.trackOutput = (flags & 1) != 0;
        packet.mode = mode.name();
        packet.conditional = (flags & 2) != 0;
        packet.alwaysActive = (flags & 4) != 0;
        return packet;
    }

    @NetworkEnum
    public enum Mode {
        SEQUENCE, AUTO, REDSTONE
    }
}
