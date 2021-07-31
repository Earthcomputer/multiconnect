package net.earthcomputer.multiconnect.mixin.bridge;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.earthcomputer.multiconnect.transformer.TransformerByteBuf;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.PacketEncoder;
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

    @Unique private final ThreadLocal<Packet<?>> packet = new ThreadLocal<>();
    @Unique private final ThreadLocal<ChannelHandlerContext> context = new ThreadLocal<>();

    @Inject(method = "encode", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;writeVarInt(I)Lnet/minecraft/network/PacketByteBuf;"))
    private void captureLocals(ChannelHandlerContext context, Packet<?> packet, ByteBuf byteBuf, CallbackInfo ci) {
        this.packet.set(packet);
        this.context.set(context);
    }

    @ModifyVariable(method = "encode", ordinal = 0, at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;writeVarInt(I)Lnet/minecraft/network/PacketByteBuf;", shift = At.Shift.AFTER))
    private PacketByteBuf transformPacketByteBuf(PacketByteBuf buf) {
        Packet<?> packet = this.packet.get();
        this.packet.set(null);
        ChannelHandlerContext context = this.context.get();
        this.context.set(null);

        if (side != NetworkSide.SERVERBOUND) {
            return buf;
        }

        TransformerByteBuf transformerBuf = new TransformerByteBuf(buf, context);
        transformerBuf.writeTopLevelType(packet.getClass());

        return transformerBuf;
    }
}
