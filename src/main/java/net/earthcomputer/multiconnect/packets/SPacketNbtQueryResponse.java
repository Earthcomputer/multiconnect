package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.minecraft.nbt.NbtCompound;

@MessageVariant
public class SPacketNbtQueryResponse {
    public int transactionId;
    public NbtCompound nbt;
}
