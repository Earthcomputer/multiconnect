package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Registry;
import net.earthcomputer.multiconnect.ap.Registries;

import java.util.UUID;

@MessageVariant
public class SPacketPaintingSpawn {
    public int entityId;
    public UUID uuid;
    @Registry(Registries.MOTIVE)
    public int motive;
    public CommonTypes.BlockPos pos;
    public byte direction;
}
