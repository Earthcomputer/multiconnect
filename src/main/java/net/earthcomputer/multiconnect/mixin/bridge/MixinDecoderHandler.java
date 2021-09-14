package net.earthcomputer.multiconnect.mixin.bridge;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.generic.ChunkDataTranslator;
import net.earthcomputer.multiconnect.protocols.generic.INetworkState;
import net.earthcomputer.multiconnect.protocols.generic.IUserDataHolder;
import net.earthcomputer.multiconnect.protocols.generic.TypedMap;
import net.earthcomputer.multiconnect.transformer.TransformerByteBuf;
import net.minecraft.SharedConstants;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.DecoderHandler;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.PacketByteBuf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(DecoderHandler.class)
public class MixinDecoderHandler {

    @Shadow @Final private NetworkSide side;

    @Unique private final ThreadLocal<ChannelHandlerContext> context = new ThreadLocal<>();
    @Unique private final ThreadLocal<Integer> packetId = new ThreadLocal<>();
    @Unique private final ThreadLocal<Boolean> canceled = new ThreadLocal<>();
    @Unique private final ThreadLocal<TypedMap> userData = new ThreadLocal<>();

    @Inject(method = "decode", at = @At(value = "INVOKE", target = "Lio/netty/channel/ChannelHandlerContext;channel()Lio/netty/channel/Channel;", ordinal = 0, remap = false), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onDecodeHead(ChannelHandlerContext context, ByteBuf buf, List<Object> output, CallbackInfo ci, PacketByteBuf packetBuf, int packetId) {
        this.context.set(context);
        this.packetId.set(packetId);
        this.canceled.set(false);
    }

    @ModifyVariable(method = "decode", ordinal = 0, at = @At(value = "INVOKE", target = "Lio/netty/channel/Channel;attr(Lio/netty/util/AttributeKey;)Lio/netty/util/Attribute;", ordinal = 0, remap = false))
    private PacketByteBuf transformPacketByteBuf(PacketByteBuf buf) {
        ChannelHandlerContext context = this.context.get();
        this.context.set(null);

        if (side != NetworkSide.CLIENTBOUND) {
            return buf;
        }

        //noinspection ConstantConditions
        INetworkState state = (INetworkState) (Object) context.channel().attr(ClientConnection.PROTOCOL_ATTRIBUTE_KEY).get();
        //noinspection ConstantConditions
        var packetInfo = state.getPacketHandlers().get(NetworkSide.CLIENTBOUND).multiconnect_getPacketInfoById(packetId.get());
        boolean versionMismatch = ConnectionInfo.protocolVersion != SharedConstants.getProtocolVersion();
        
        if (versionMismatch && ConnectionInfo.protocol.shouldTranslateAsync(packetInfo.getPacketClass())) {
            byte[] data = new byte[buf.readableBytes()];
            buf.readBytes(data);
            ChunkDataTranslator.asyncTranslatePacket(context, packetInfo, data);
            canceled.set(true);
            return buf;
        }

        TransformerByteBuf transformerBuf = new TransformerByteBuf(buf, context);
        userData.set(new TypedMap());
        transformerBuf.readTopLevelType(packetInfo.getPacketClass(), userData.get());
        return transformerBuf;
    }

    @Inject(method = "decode", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkState;getPacketHandler(Lnet/minecraft/network/NetworkSide;ILnet/minecraft/network/PacketByteBuf;)Lnet/minecraft/network/Packet;", ordinal = 0), cancellable = true)
    private void cancelDecode(CallbackInfo ci) {
        if (canceled.get()) {
            ci.cancel();
        }
    }

    @ModifyArg(method = "decode", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", remap = false))
    private Object onAddPacket(Object packet) {
        if (packet instanceof IUserDataHolder holder) {
            holder.multiconnect_getUserData().putAll(userData.get());
        }
        userData.set(null);
        return packet;
    }

}
