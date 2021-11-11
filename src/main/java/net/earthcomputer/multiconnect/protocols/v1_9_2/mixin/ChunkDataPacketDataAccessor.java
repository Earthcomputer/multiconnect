package net.earthcomputer.multiconnect.protocols.v1_9_2.mixin;

import net.minecraft.class_6603;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(class_6603.class)
public interface ChunkDataPacketDataAccessor {
    @Accessor("field_34865")
    List<class_6603.class_6604> getBlockEntities();
}
