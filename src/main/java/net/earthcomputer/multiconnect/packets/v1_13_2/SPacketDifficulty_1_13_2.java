package net.earthcomputer.multiconnect.packets.v1_13_2;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketDifficulty;

@MessageVariant(maxVersion = Protocols.V1_13_2)
public class SPacketDifficulty_1_13_2 implements SPacketDifficulty {
    @Type(Types.UNSIGNED_BYTE)
    public int difficulty;
}
