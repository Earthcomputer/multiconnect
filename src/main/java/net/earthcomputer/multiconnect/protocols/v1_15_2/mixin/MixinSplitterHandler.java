package net.earthcomputer.multiconnect.protocols.v1_15_2.mixin;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.CorruptedFrameException;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.SplitterHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(SplitterHandler.class)
public class MixinSplitterHandler {

    @Inject(method = "decode", at = @At("HEAD"), cancellable = true)
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list, CallbackInfo ci) throws Exception {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_15_2) {
            byteBuf.markReaderIndex();
            byte[] bs = new byte[3];

            for(int i = 0; i < bs.length; ++i) {
                if (!byteBuf.isReadable()) {
                    byteBuf.resetReaderIndex();
                    ci.cancel();
                    return;
                }

                bs[i] = byteBuf.readByte();
                if (bs[i] >= 0) {
                    PacketByteBuf packetByteBuf = new PacketByteBuf(Unpooled.wrappedBuffer(bs));

                    try {
                        int j = packetByteBuf.readVarInt();
                        if (byteBuf.readableBytes() >= j) {
                            list.add(byteBuf.readBytes(j));
                            ci.cancel();
                            return;
                        }

                        byteBuf.resetReaderIndex();
                    } finally {
                        packetByteBuf.release();
                    }

                    ci.cancel();
                    return;
                }
            }

            throw new CorruptedFrameException("length wider than 21-bit");
        }
    }
}
