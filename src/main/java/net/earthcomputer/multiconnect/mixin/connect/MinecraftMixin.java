package net.earthcomputer.multiconnect.mixin.connect;

import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.debug.DebugUtils;
import net.earthcomputer.multiconnect.impl.MulticonnectConfig;
import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.earthcomputer.multiconnect.protocols.v1_18.SignedChatScreen;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Inject(method = "clearLevel(Lnet/minecraft/client/gui/screens/Screen;)V", at = @At("RETURN"))
    public void onDisconnect(Screen screen, CallbackInfo ci) {
        if (ConnectionInfo.protocol != null) {
            ConnectionInfo.protocol.disable();
        }
        ConnectionInfo.protocolVersion = SharedConstants.getCurrentVersion().getProtocolVersion();
        ConnectionInfo.protocol = ProtocolRegistry.get(ConnectionInfo.protocolVersion);
        ConnectionInfo.protocol.setup();
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
