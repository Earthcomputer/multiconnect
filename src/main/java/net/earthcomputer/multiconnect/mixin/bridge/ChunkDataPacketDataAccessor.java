package net.earthcomputer.multiconnect.mixin.bridge;

import net.minecraft.class_6603;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(class_6603.class)
public interface ChunkDataPacketDataAccessor {
    @Accessor("field_34864")
    @Mutable
    void setData(byte[] data);
}
