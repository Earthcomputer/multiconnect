package net.earthcomputer.multiconnect.impl.via;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.exception.CancelCodecException;
import com.viaversion.viaversion.exception.CancelEncoderException;
import com.viaversion.viaversion.util.PipelineUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

@ChannelHandler.Sharable
public class MulticonnectServerboundTranslator extends MessageToMessageEncoder<ByteBuf> {
    private final UserConnection info;

    public MulticonnectServerboundTranslator(UserConnection info) {
        this.info = info;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (!info.checkOutgoingPacket()) {
            throw CancelEncoderException.generate(null);
        }
        if (!info.shouldTransformPacket()) {
            out.add(in.retain());
            return;
        }

        ByteBuf transformedBuf = ctx.alloc().buffer().writeBytes(in);
        try {
            info.transformOutgoing(transformedBuf, CancelEncoderException::generate);
            out.add(transformedBuf.retain());
        } finally {
            transformedBuf.release();
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (!PipelineUtil.containsCause(cause, CancelCodecException.class)) {
            super.exceptionCaught(ctx, cause);
        }
    }
}
