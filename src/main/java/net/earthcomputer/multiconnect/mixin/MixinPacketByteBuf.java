package net.earthcomputer.multiconnect.mixin;

import io.netty.buffer.ByteBuf;
import net.earthcomputer.multiconnect.impl.IPacketByteBuf;
import net.minecraft.util.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PacketByteBuf.class)
public abstract class MixinPacketByteBuf implements IPacketByteBuf {

    @Accessor
    @Override
    public abstract ByteBuf getParent();
}
