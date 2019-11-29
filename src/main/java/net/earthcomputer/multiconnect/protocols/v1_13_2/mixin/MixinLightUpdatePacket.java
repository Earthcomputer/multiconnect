package net.earthcomputer.multiconnect.protocols.v1_13_2.mixin;

import net.earthcomputer.multiconnect.protocols.v1_13_2.ILightUpdatePacket;
import net.minecraft.client.network.packet.LightUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LightUpdateS2CPacket.class)
public abstract class MixinLightUpdatePacket implements ILightUpdatePacket {

    @Accessor
    @Override
    public abstract void setChunkX(int chunkX);

    @Accessor
    @Override
    public abstract void setChunkZ(int chunkZ);

    @Accessor
    @Override
    public abstract void setSkylightMask(int skylightMask);

    @Accessor
    @Override
    public abstract void setBlocklightMask(int blocklightMask);
}
