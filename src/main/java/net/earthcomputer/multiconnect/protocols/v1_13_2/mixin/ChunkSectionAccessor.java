package net.earthcomputer.multiconnect.protocols.v1_13_2.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.block.BlockState;
import net.minecraft.util.palette.IPalette;
import net.minecraft.world.chunk.ChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkSection.class)
public interface ChunkSectionAccessor {
    @Accessor("REGISTRY_PALETTE")
    static IPalette<BlockState> getPalette() {
        return MixinHelper.fakeInstance();
    }
}
