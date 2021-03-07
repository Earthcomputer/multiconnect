package net.earthcomputer.multiconnect.mixin.connect;

import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient {

    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("RETURN"))
    public void onDisconnect(Screen screen, CallbackInfo ci) {
        if (ConnectionInfo.protocol != null) {
            ConnectionInfo.protocol.disable();
        }
        ConnectionInfo.protocolVersion = SharedConstants.getGameVersion().getProtocolVersion();
        ConnectionInfo.protocol = ProtocolRegistry.get(ConnectionInfo.protocolVersion);
        ConnectionInfo.protocol.setup(false);
    }
}
