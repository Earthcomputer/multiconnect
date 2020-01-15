package net.earthcomputer.multiconnect.impl;

import net.minecraft.client.search.SearchManager;

public interface IMinecraftClient {

    void callInitializeSearchableContainers();

    SearchManager getSearchManager();

}
