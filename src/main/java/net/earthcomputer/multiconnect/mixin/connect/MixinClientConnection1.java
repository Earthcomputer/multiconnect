package net.earthcomputer.multiconnect.mixin.connect;

import io.netty.channel.ChannelHandler;
import net.earthcomputer.multiconnect.impl.DebugUtils;
import net.minecraft.network.DecoderHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(targets = "net.minecraft.network.ClientConnection$1")
public class MixinClientConnection1 {
    @ModifyArg(method = "initChannel(Lio/netty/channel/Channel;)V",
            index = 1,
            remap = false,
            at = @At(value = "INVOKE", target = "Lio/netty/channel/ChannelPipeline;addLast(Ljava/lang/String;Lio/netty/channel/ChannelHandler;)Lio/netty/channel/ChannelPipeline;", ordinal = 0),
            slice = @Slice(from = @At(value = "CONSTANT", args = "stringValue=decoder")))
    private ChannelHandler wrapDecoderHandler(ChannelHandler decoderHandler) {
        return new DebugUtils.DebugDecoderHandler((DecoderHandler) decoderHandler);
    }
}
