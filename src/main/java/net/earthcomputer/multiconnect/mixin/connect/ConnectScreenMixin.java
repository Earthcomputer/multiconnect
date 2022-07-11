package net.earthcomputer.multiconnect.mixin.connect;

import net.earthcomputer.multiconnect.connect.IConnectScreen;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicReference;

@Mixin(ConnectScreen.class)
public abstract class ConnectScreenMixin implements IConnectScreen {

    @Unique private final AtomicReference<Connection> multiconnect_versionRequestConnection = new AtomicReference<>();

    @Accessor
    @Override
    public abstract Screen getParent();

    @Override
    public void multiconnect_setVersionRequestConnection(@Nullable Connection connection) {
        multiconnect_versionRequestConnection.set(connection);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        Connection versionRequestConnection = this.multiconnect_versionRequestConnection.get();
        if (versionRequestConnection != null) {
            if (versionRequestConnection.isConnected()) {
                versionRequestConnection.tick();
            } else {
                versionRequestConnection.handleDisconnection();
            }
        }
    }

    @Inject(method = "method_19800", remap = false, at = @At("HEAD"))
    private void onDisconnectButtonPressed(CallbackInfo ci) {
        Connection versionRequestConnection = this.multiconnect_versionRequestConnection.get();
        if (versionRequestConnection != null) {
            versionRequestConnection.disconnect(Component.translatable("connect.aborted"));
        }
    }
}
