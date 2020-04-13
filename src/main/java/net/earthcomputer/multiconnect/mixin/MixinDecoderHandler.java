package net.earthcomputer.multiconnect.mixin;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.transformer.TransformerByteBuf;
import net.minecraft.network.IPacket;
import net.minecraft.network.NettyPacketDecoder;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.PacketDirection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(NettyPacketDecoder.class)
public class MixinDecoderHandler {

    @Shadow @Final private PacketDirection direction;

    @Unique private ThreadLocal<ChannelHandlerContext> context = new ThreadLocal<>();

    @Inject(method = "decode", at = @At("HEAD"))
    private void onDecodeHead(ChannelHandlerContext context, ByteBuf buf, List<Object> output, CallbackInfo ci) {
        this.context.set(context);
    }

    @ModifyVariable(method = "decode", ordinal = 0, at = @At(value = "STORE", ordinal = 0))
    private PacketBuffer transformPacketBuffer(PacketBuffer buf) {
        if (direction == PacketDirection.CLIENTBOUND)
            buf = new TransformerByteBuf(buf, context.get());
        context.set(null);
        return buf;
    }

    @Inject(method = "decode", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/IPacket;readPacketData(Lnet/minecraft/network/PacketBuffer;)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    private void postDecode(ChannelHandlerContext context, ByteBuf buf, List<Object> output, CallbackInfo ci, PacketBuffer packetBuf, int packetId, IPacket<?> packet) {
        if (!((TransformerByteBuf) packetBuf).canDecodeAsync(packet.getClass())) {
            ConnectionInfo.resourceReloadLock.readLock().unlock();
        }
    }

}
