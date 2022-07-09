package net.earthcomputer.multiconnect.protocols.v1_10.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {

    @Shadow protected EditBox input;

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/EditBox;setMaxLength(I)V", shift = At.Shift.AFTER))
    private void changeMaxChatLength(CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_10) {
            input.setMaxLength(100);
        }
    }

}
