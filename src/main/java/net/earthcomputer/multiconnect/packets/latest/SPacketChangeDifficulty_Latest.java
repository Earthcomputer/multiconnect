package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketChangeDifficulty;

@MessageVariant(minVersion = Protocols.V1_14)
public class SPacketChangeDifficulty_Latest implements SPacketChangeDifficulty {
    @Type(Types.UNSIGNED_BYTE)
    public int difficulty;
    @Introduce(booleanValue = false)
    public boolean locked;
}
