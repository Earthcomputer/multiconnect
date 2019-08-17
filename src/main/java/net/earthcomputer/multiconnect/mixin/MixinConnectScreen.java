package net.earthcomputer.multiconnect.mixin;

import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.impl.IConnectScreen;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ConnectScreen.class)
public abstract class MixinConnectScreen implements IConnectScreen {

    @Inject(method = "connect", at = @At("HEAD"))
    public void onConnect(String ip, int port, CallbackInfo ci) {
        ConnectionInfo.ip = ip;
        ConnectionInfo.port = port;
    }

    @Accessor
    @Override
    public abstract boolean isConnectingCancelled();

    @Accessor
    @Override
    public abstract Screen getParent();
}
