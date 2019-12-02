package net.earthcomputer.multiconnect.impl;

import net.minecraft.client.color.item.ItemColorProvider;
import net.minecraft.util.IdList;

public interface IItemColors {

    IdList<ItemColorProvider> getProviders();

}
