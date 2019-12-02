package net.earthcomputer.multiconnect.impl;

import net.minecraft.client.color.block.BlockColorProvider;
import net.minecraft.util.IdList;

public interface IBlockColors {

    IdList<BlockColorProvider> getProviders();

}
