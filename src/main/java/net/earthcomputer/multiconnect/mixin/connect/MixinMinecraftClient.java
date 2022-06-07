package net.earthcomputer.multiconnect.mixin.connect;

import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.impl.DebugUtils;
import net.earthcomputer.multiconnect.impl.MulticonnectConfig;
import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.earthcomputer.multiconnect.protocols.v1_18_2.SignedChatScreen;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
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

    @ModifyVariable(method = "setScreen", at = @At("HEAD"), argsOnly = true)
    private Screen modifyScreen(Screen screen) {
        if (screen instanceof TitleScreen) {
            if (MulticonnectConfig.INSTANCE.allowOldUnsignedChat == null) {
                return new SignedChatScreen(screen);
            }
        }

        if (screen instanceof DisconnectedScreen && DebugUtils.wasRareBugReportedRecently()) {
            return DebugUtils.createRareBugScreen(screen);
        }

        return screen;
    }
}
