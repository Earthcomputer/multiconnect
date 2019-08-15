package net.earthcomputer.multiconnect.mixin;

import net.earthcomputer.multiconnect.ConnectionInfo;
import net.earthcomputer.multiconnect.protocol.Protocols;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {

    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("RETURN"))
    public void onDisconnect(Screen screen, CallbackInfo ci) {
        ConnectionInfo.ip = null;
        ConnectionInfo.port = -1;
        ConnectionInfo.protocolVersion = SharedConstants.getGameVersion().getProtocolVersion();
        ConnectionInfo.protocol = Protocols.get(ConnectionInfo.protocolVersion);
    }

}
