package net.earthcomputer.multiconnect.mixin.connect;

import net.earthcomputer.multiconnect.connect.IConnectScreen;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.ClientConnection;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicReference;

@Mixin(ConnectScreen.class)
public abstract class MixinConnectScreen implements IConnectScreen {

    @Unique private final AtomicReference<ClientConnection> versionRequestConnection = new AtomicReference<>();

    @Accessor
    @Override
    public abstract Screen getParent();

    @Override
    public void multiconnect_setVersionRequestConnection(ClientConnection connection) {
        versionRequestConnection.set(connection);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        ClientConnection versionRequestConnection = this.versionRequestConnection.get();
        if (versionRequestConnection != null) {
            if (versionRequestConnection.isOpen()) {
                versionRequestConnection.tick();
            } else {
                versionRequestConnection.handleDisconnection();
            }
        }
    }

    @Inject(method = "method_19800", remap = false, at = @At("HEAD"))
    private void onDisconnectButtonPressed(CallbackInfo ci) {
        ClientConnection versionRequestConnection = this.versionRequestConnection.get();
        if (versionRequestConnection != null) {
            versionRequestConnection.disconnect(Text.translatable("connect.aborted"));
        }
    }
}
