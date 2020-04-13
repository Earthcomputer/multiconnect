package net.earthcomputer.multiconnect.mixin;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.transformer.TransformerByteBuf;
import net.minecraft.network.IPacket;
import net.minecraft.network.NettyPacketEncoder;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.PacketDirection;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NettyPacketEncoder.class)
public class MixinEncoderHandler {

    @Shadow @Final private PacketDirection direction;

    @Unique private ThreadLocal<ChannelHandlerContext> context = new ThreadLocal<>();

    @Unique private ThreadLocal<PacketBuffer> buf = new ThreadLocal<>();

    @Inject(method = "encode", at = @At(value = "JUMP", opcode = Opcodes.IFNONNULL, ordinal = 1))
    private void onEncodeHead(ChannelHandlerContext context, IPacket<?> packet, ByteBuf buf, CallbackInfo ci) {
        this.context.set(context);
    }

    @ModifyVariable(method = "encode", ordinal = 0, at = @At(value = "STORE", ordinal = 0))
    private PacketBuffer transformPacketBuffer(PacketBuffer buf) {
        if (direction == PacketDirection.SERVERBOUND)
            buf = new TransformerByteBuf(buf, context.get());
        context.set(null);
        this.buf.set(buf);
        return buf;
    }

    @Inject(method = "encode", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/IPacket;writePacketData(Lnet/minecraft/network/PacketBuffer;)V", shift = At.Shift.AFTER))
    private void postWrite(ChannelHandlerContext context, IPacket<?> packet, ByteBuf buf, CallbackInfo ci) {
        if (!((TransformerByteBuf) this.buf.get()).canEncodeAsync(packet.getClass())) {
            ConnectionInfo.resourceReloadLock.readLock().unlock();
        }
        this.buf.set(null);
    }

    @Inject(method = "encode", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/IPacket;shouldSkipErrors()Z"))
    private void postWriteError(ChannelHandlerContext context, IPacket<?> packet, ByteBuf buf, CallbackInfo ci) {
        if (!((TransformerByteBuf) this.buf.get()).canEncodeAsync(packet.getClass())) {
            ConnectionInfo.resourceReloadLock.readLock().unlock();
        }
        this.buf.set(null);
    }

}
