package net.earthcomputer.multiconnect.impl;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.NetworkManager;

public interface IConnectScreen {

    boolean isConnectingCancelled();

    Screen getPreviousGuiScreen();

    void multiconnect_setVersionRequestConnection(NetworkManager connection);

}
