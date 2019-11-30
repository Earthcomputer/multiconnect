package net.earthcomputer.multiconnect.impl;

import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;

public interface IMinecraftClient {

    void setBlockColorMap(BlockColors blockColorMap);

    void setItemColorMap(ItemColors itemColorMap);

}
