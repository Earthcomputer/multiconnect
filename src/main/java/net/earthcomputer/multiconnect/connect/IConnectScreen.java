package net.earthcomputer.multiconnect.connect;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.ClientConnection;

public interface IConnectScreen {

    boolean isConnectingCancelled();

    Screen getParent();
    /**
     *  Sets the version request connection.
     *  @param connection - The new connection
    */
    void multiconnect_setVersionRequestConnection(ClientConnection connection);

}
