package net.earthcomputer.multiconnect.impl;

import net.minecraft.client.util.SearchTreeManager;

public interface IMinecraftClient {

    void callPopulateSearchTreeManager();

    SearchTreeManager getSearchTreeManager();

}
