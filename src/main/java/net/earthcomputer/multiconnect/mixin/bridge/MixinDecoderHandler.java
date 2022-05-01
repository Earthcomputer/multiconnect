package net.earthcomputer.multiconnect.mixin.bridge;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.earthcomputer.multiconnect.impl.PacketSystem;
import net.minecraft.network.DecoderHandler;
import net.minecraft.network.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(DecoderHandler.class)
public class MixinDecoderHandler {
    @Inject(method = "decode", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", shift = At.Shift.AFTER, remap = false))
    private void onAddPacket(ChannelHandlerContext ctx, ByteBuf buf, List<Object> outPackets, CallbackInfo ci) {
        PacketSystem.Internals.setUserData(
                (Packet<?>) outPackets.get(outPackets.size() - 1),
                PacketSystem.Internals.getUserData(buf)
        );
    }
}
