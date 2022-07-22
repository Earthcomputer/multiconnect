package net.earthcomputer.multiconnect.protocols.v1_19.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.impl.MulticonnectConfig;
import net.minecraft.client.multiplayer.chat.ChatListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChatListener.class)
public class ChatListenerMixin {
    @ModifyVariable(method = "handleChatMessage", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
    private boolean modifyOnlyShowSecureChat(boolean original) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_19 && MulticonnectConfig.INSTANCE.allowOldUnsignedChat == Boolean.TRUE) {
            return false;
        }
        return original;
    }
}
