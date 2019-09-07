package net.earthcomputer.multiconnect.mixin;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.earthcomputer.multiconnect.impl.TransformerByteBuf;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketEncoder;
import net.minecraft.util.PacketByteBuf;
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

    @Inject(method = "method_10838", at = @At(value = "JUMP", opcode = Opcodes.IFNONNULL, ordinal = 1))
    private void onEncodeHead(ChannelHandlerContext context, Packet<?> packet, ByteBuf buf, CallbackInfo ci) {
        this.context.set(context);
    }

    @ModifyVariable(method = "method_10838", ordinal = 0, at = @At(value = "STORE", ordinal = 0))
    private PacketByteBuf transformPacketByteBuf(PacketByteBuf buf) {
        if (side == NetworkSide.SERVERBOUND)
            buf = new TransformerByteBuf(buf, context.get());
        context.set(null);
        return buf;
    }

}
