package net.earthcomputer.multiconnect.mixin;

import net.earthcomputer.multiconnect.impl.IBlockColors;
import net.minecraft.client.color.block.BlockColorProvider;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.util.IdList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockColors.class)
public abstract class MixinBlockColors implements IBlockColors {

    @Accessor
    @Override
    public abstract IdList<BlockColorProvider> getProviders();
}
