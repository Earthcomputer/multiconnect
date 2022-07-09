package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.minecraft.nbt.CompoundTag;

@MessageVariant
public class SPacketNbtQueryResponse {
    public int transactionId;
    public CompoundTag nbt;
}
