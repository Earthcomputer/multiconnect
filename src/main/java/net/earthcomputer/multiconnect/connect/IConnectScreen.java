package net.earthcomputer.multiconnect.connect;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.ClientConnection;

public interface IConnectScreen {
    Screen getParent();

    void multiconnect_setVersionRequestConnection(ClientConnection connection);

}
