package net.earthcomputer.multiconnect.mixin;

import net.earthcomputer.multiconnect.impl.IBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.sound.BlockSoundGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Block.Settings.class)
public abstract class MixinBlockSettings implements IBlockSettings {

    @Invoker
    @Override
    public abstract Block.Settings callBreakInstantly();

    @Invoker
    @Override
    public abstract Block.Settings callSounds(BlockSoundGroup soundGroup);

    @Invoker
    @Override
    public abstract Block.Settings callStrength(float strength);
}
