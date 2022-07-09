package net.earthcomputer.multiconnect.packets.v1_12_2;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketAddPainting;

import java.util.UUID;

@MessageVariant(maxVersion = Protocols.V1_12_2)
public class SPacketAddPainting_1_12_2 implements SPacketAddPainting {
    public int entityId;
    public UUID uuid;
    public String motive;
    public CommonTypes.BlockPos pos;
    public byte direction;
}
