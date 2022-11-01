package net.earthcomputer.multiconnect.protocols.generic;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.exception.CancelCodecException;
import com.viaversion.viaversion.exception.CancelEncoderException;
import com.viaversion.viaversion.exception.InformativeException;
import com.viaversion.viaversion.util.PipelineUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

@ChannelHandler.Sharable
public class MulticonnectClientboundTranslator extends MessageToMessageDecoder<ByteBuf> {
    private final UserConnection info;

    public MulticonnectClientboundTranslator(UserConnection info) {
        this.info = info;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (!info.checkIncomingPacket()) {
            throw CancelEncoderException.generate(null);
        }
        if (!info.shouldTransformPacket()) {
            out.add(in.retain());
            return;
        }

        ByteBuf transformedBuf = ctx.alloc().buffer().writeBytes(in);
        try {
            info.transformIncoming(transformedBuf, CancelEncoderException::generate);
            out.add(transformedBuf.retain());
        } finally {
            transformedBuf.release();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (PipelineUtil.containsCause(cause, CancelCodecException.class)) {
            return;
        }

        if ((PipelineUtil.containsCause(cause, InformativeException.class) && info.getProtocolInfo().getState() != State.HANDSHAKE)
            || Via.getManager().debugHandler().enabled()
        ) {
            cause.printStackTrace();
        }

        super.exceptionCaught(ctx, cause);
    }
}
