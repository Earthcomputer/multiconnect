package net.earthcomputer.multiconnect.mixin.bridge;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.earthcomputer.multiconnect.impl.PacketSystem;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketEncoder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PacketEncoder.class)
public class MixinEncoderHandler {
    @Inject(method = "encode(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/Packet;Lio/netty/buffer/ByteBuf;)V", at = @At("HEAD"))
    private void onEncode(ChannelHandlerContext ctx, Packet<?> packet, ByteBuf buf, CallbackInfo ci) {
        PacketSystem.Internals.setUserData(buf, PacketSystem.getUserData(packet));
    }
}
