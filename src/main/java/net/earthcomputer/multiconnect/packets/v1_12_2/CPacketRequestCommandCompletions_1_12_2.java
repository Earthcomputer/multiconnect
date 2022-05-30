package net.earthcomputer.multiconnect.packets.v1_12_2;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CPacketRequestCommandCompletions;
import net.earthcomputer.multiconnect.packets.CommonTypes;

import java.util.Optional;

@MessageVariant(maxVersion = Protocols.V1_12_2)
public class CPacketRequestCommandCompletions_1_12_2 implements CPacketRequestCommandCompletions {
    public String command;
    public boolean isFromCommandBlock;
    public Optional<CommonTypes.BlockPos> target;
}
