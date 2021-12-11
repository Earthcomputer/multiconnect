package net.earthcomputer.multiconnect.protocols.generic;

import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;

public interface IClientPlayNetworkHandler {
    void multiconnect_onAfterChunkData(ChunkDataS2CPacket packet);
}
