package net.earthcomputer.multiconnect.impl;

import net.minecraft.block.Block;
import net.minecraft.sound.BlockSoundGroup;

public interface IBlockSettings {

    Block.Settings callBreakInstantly();

    Block.Settings callSounds(BlockSoundGroup soundGroup);

    Block.Settings callStrength(float strength);

}
