package net.earthcomputer.multiconnect.protocols.generic;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public interface ICustomPayloadC2SPacket {
    Identifier multiconnect_getChannel();
    PacketByteBuf multiconnect_getData();
    boolean multiconnect_isBlocked();
    void multiconnect_unblock();
}
