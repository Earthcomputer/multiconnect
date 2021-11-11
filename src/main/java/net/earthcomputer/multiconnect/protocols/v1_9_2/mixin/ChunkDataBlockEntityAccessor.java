package net.earthcomputer.multiconnect.protocols.v1_9_2.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.class_6603;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(class_6603.class_6604.class)
public interface ChunkDataBlockEntityAccessor {
    @Invoker("<init>")
    static class_6603.class_6604 createChunkDataBlockEntity(int i, int j, BlockEntityType<?> blockEntityType, @Nullable NbtCompound nbtCompound) {
        return MixinHelper.fakeInstance();
    }
}
