package net.earthcomputer.multiconnect.impl;

import io.netty.buffer.ByteBuf;

public interface IPacketByteBuf {

    ByteBuf getParent();

}
