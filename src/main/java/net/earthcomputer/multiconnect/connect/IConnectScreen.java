package net.earthcomputer.multiconnect.connect;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.Connection;
import org.jetbrains.annotations.Nullable;

public interface IConnectScreen {
    Screen getParent();

    void multiconnect_setVersionRequestConnection(@Nullable Connection connection);

}
