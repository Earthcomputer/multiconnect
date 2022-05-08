package net.earthcomputer.multiconnect.connect;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.ClientConnection;

public interface IConnectScreen {
    /**
        @return If the connecting is cancelled, if this is true, it will stop trying to connect.
    */
    boolean isConnectingCancelled();
    /**
        @return the parent screen, where to return
    */
    Screen getParent();
    /**
        Sets the version request connection.
        @param connection - The new connection
     */
    void multiconnect_setVersionRequestConnection(ClientConnection connection);

}
