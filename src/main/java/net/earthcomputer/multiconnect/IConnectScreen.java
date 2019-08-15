package net.earthcomputer.multiconnect;

import net.minecraft.client.gui.screen.Screen;

public interface IConnectScreen {

    boolean isConnectingCancelled();

    Screen getParent();

}
