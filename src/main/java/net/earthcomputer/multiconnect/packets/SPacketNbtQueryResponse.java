package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;
import net.minecraft.nbt.NbtCompound;

@Message
public class SPacketNbtQueryResponse {
    public int transactionId;
    public NbtCompound nbt;
}
