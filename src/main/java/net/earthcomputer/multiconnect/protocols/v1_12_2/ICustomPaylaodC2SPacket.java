package net.earthcomputer.multiconnect.protocols.v1_12_2;

import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

public interface ICustomPaylaodC2SPacket {

    Identifier getChannel();

    PacketByteBuf getData();

}
