package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.util.datafix.fixes.ChunkPaletteFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.BitSet;

@Mixin(ChunkPaletteFormat.class)
public interface ChunkPalettedStorageFixAccessor {

    @Accessor("VIRTUAL")
    static BitSet getBlocksNeedingSideUpdate() {
        return MixinHelper.fakeInstance();
    }

}
