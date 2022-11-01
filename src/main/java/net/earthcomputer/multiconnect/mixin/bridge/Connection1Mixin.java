package net.earthcomputer.multiconnect.mixin.bridge;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.connection.UserConnectionImpl;
import com.viaversion.viaversion.protocol.ProtocolPipelineImpl;
import io.netty.channel.Channel;
import net.earthcomputer.multiconnect.debug.DebugUtils;
import net.earthcomputer.multiconnect.protocols.generic.MulticonnectClientboundTranslator;
import net.earthcomputer.multiconnect.protocols.generic.MulticonnectServerboundTranslator;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.network.Connection$1")
public class Connection1Mixin {
    @Inject(method = "initChannel", at = @At("RETURN"))
    private void onInitChannel(Channel channel, CallbackInfo ci) {
        // Singleplayer doesnt include encoding
        boolean enableTranslation = !Minecraft.getInstance().hasSingleplayerServer() && !DebugUtils.SKIP_TRANSLATION;
        if (!enableTranslation) {
            return;
        }

        UserConnection info = new UserConnectionImpl(channel, true);
        new ProtocolPipelineImpl(info);
        channel.pipeline()
            .addBefore("encoder", "multiconnect_serverbound_translator", new MulticonnectServerboundTranslator(info))
            .addBefore("decoder", "multiconnect_clientbound_translator", new MulticonnectClientboundTranslator(info));
    }
}
