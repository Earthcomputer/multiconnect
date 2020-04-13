package net.earthcomputer.multiconnect.mixin;

import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.impl.IConnectScreen;
import net.minecraft.client.gui.screen.ConnectingScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.NetworkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicReference;

@Mixin(ConnectingScreen.class)
public abstract class MixinConnectScreen implements IConnectScreen {

    @Unique private AtomicReference<NetworkManager> versionRequestConnection = new AtomicReference<>();

    @Inject(method = "connect", at = @At("HEAD"))
    public void onConnect(String ip, int port, CallbackInfo ci) {
        ConnectionInfo.ip = ip;
        ConnectionInfo.port = port;
    }

    @Accessor("cancel")
    @Override
    public abstract boolean isConnectingCancelled();

    @Accessor
    @Override
    public abstract Screen getPreviousGuiScreen();

    @Override
    public void multiconnect_setVersionRequestConnection(NetworkManager connection) {
        versionRequestConnection.set(connection);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        NetworkManager versionRequestConnection = this.versionRequestConnection.get();
        if (versionRequestConnection != null) {
            if (versionRequestConnection.isChannelOpen())
                versionRequestConnection.tick();
            else
                versionRequestConnection.handleDisconnection();
        }
    }
}
