package net.earthcomputer.multiconnect.packets.v1_17_1;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketBlockEntityData;
import net.minecraft.nbt.CompoundTag;

@MessageVariant(maxVersion = Protocols.V1_17_1)
public class SPacketBlockEntityData_1_17_1 implements SPacketBlockEntityData {
    public CommonTypes.BlockPos pos;
    public byte blockEntityType; // not from the block entity registry
    public CompoundTag data;
}
