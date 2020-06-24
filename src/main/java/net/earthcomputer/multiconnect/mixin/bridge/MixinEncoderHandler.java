package net.earthcomputer.multiconnect.mixin.bridge;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.transformer.TransformerByteBuf;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.PacketEncoder;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PacketEncoder.class)
public class MixinEncoderHandler {

    @Shadow @Final private NetworkSide side;

    @Unique private ThreadLocal<ChannelHandlerContext> context = new ThreadLocal<>();

    @Unique private ThreadLocal<PacketByteBuf> buf = new ThreadLocal<>();

    @Inject(method = "encode", at = @At(value = "JUMP", opcode = Opcodes.IFNONNULL, ordinal = 1))
    private void onEncodeHead(ChannelHandlerContext context, Packet<?> packet, ByteBuf buf, CallbackInfo ci) {
        this.context.set(context);
    }

    @ModifyVariable(method = "encode", ordinal = 0, at = @At(value = "STORE", ordinal = 0))
    private PacketByteBuf transformPacketByteBuf(PacketByteBuf buf) {
        if (side == NetworkSide.SERVERBOUND)
            buf = new TransformerByteBuf(buf, context.get());
        context.set(null);
        this.buf.set(buf);
        return buf;
    }

    @Inject(method = "encode", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Packet;write(Lnet/minecraft/network/PacketByteBuf;)V", shift = At.Shift.AFTER))
    private void postWrite(ChannelHandlerContext context, Packet<?> packet, ByteBuf buf, CallbackInfo ci) {
        if (!((TransformerByteBuf) this.buf.get()).canEncodeAsync(packet.getClass())) {
            ConnectionInfo.resourceReloadLock.readLock().unlock();
        }
        this.buf.set(null);
    }

    @Inject(method = "encode", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Packet;isWritingErrorSkippable()Z"))
    private void postWriteError(ChannelHandlerContext context, Packet<?> packet, ByteBuf buf, CallbackInfo ci) {
        if (!((TransformerByteBuf) this.buf.get()).canEncodeAsync(packet.getClass())) {
            ConnectionInfo.resourceReloadLock.readLock().unlock();
        }
        this.buf.set(null);
    }

}
