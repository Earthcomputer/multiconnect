package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.datafixer.fix.ChunkPalettedStorageFix;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.BitSet;

@Mixin(ChunkPalettedStorageFix.class)
public interface ChunkPalettedStorageFixAccessor {

    @Accessor
    static BitSet getBlocksNeedingSideUpdate() {
        return MixinHelper.fakeInstance();
    }

}
