package net.earthcomputer.multiconnect.protocols.v1_14_4.mixin;

import net.earthcomputer.multiconnect.protocols.v1_14_4.IChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ChunkDataS2CPacket.class)
public class MixinChunkDataS2C implements IChunkDataS2CPacket {
    @Unique private Biome[] biomeData_1_14_4;

    @Override
    public Biome[] get_1_14_4_biomeData() {
        return biomeData_1_14_4;
    }

    @Override
    public void set_1_14_4_biomeData(Biome[] biomeData) {
        this.biomeData_1_14_4 = biomeData;
    }
}
