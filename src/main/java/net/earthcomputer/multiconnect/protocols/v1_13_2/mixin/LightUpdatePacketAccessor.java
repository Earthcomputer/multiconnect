package net.earthcomputer.multiconnect.protocols.v1_13_2.mixin;

import net.minecraft.network.play.server.SUpdateLightPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(SUpdateLightPacket.class)
public interface LightUpdatePacketAccessor {

    @Accessor
    void setChunkX(int chunkX);

    @Accessor
    void setChunkZ(int chunkZ);

    @Accessor
    void setSkyLightUpdateMask(int skylightMask);

    @Accessor
    void setBlockLightUpdateMask(int blocklightMask);

    @Accessor
    void setBlockLightData(List<byte[]> blockLightUpdates);

    @Accessor
    void setSkyLightData(List<byte[]> skyLightUpdates);
}
