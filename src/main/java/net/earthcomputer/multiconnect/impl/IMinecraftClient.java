package net.earthcomputer.multiconnect.impl;

import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.search.SearchManager;

public interface IMinecraftClient {

    ItemColors getItemColorMap();

    void callInitializeSearchableContainers();

    SearchManager getSearchManager();

}
