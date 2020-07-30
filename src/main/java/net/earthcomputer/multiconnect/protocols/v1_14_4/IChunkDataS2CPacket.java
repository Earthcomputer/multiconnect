package net.earthcomputer.multiconnect.protocols.v1_14_4;

import net.minecraft.world.biome.Biome;

public interface IChunkDataS2CPacket {
    Biome[] get_1_14_4_biomeData();
    void set_1_14_4_biomeData(Biome[] biomeData);
}
