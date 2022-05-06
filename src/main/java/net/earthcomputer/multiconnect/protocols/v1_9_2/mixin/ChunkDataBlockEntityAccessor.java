package net.earthcomputer.multiconnect.protocols.v1_9_2.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.ChunkData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ChunkData.BlockEntityData.class)
public interface ChunkDataBlockEntityAccessor {
    @Invoker("<init>")
    static ChunkData.BlockEntityData createChunkDataBlockEntity(int i, int j, BlockEntityType<?> blockEntityType, @Nullable NbtCompound nbtCompound) {
        return MixinHelper.fakeInstance();
    }
}
