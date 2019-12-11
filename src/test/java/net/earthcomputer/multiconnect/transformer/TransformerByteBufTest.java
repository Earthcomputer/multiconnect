package net.earthcomputer.multiconnect.transformer;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.impl.INetworkState;
import net.minecraft.SharedConstants;
import net.minecraft.client.network.packet.LoginHelloS2CPacket;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.NetworkState;
import net.minecraft.network.Packet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TransformerByteBufTest {

    @BeforeEach
    public void beforeEach() {
        ConnectionInfo.protocolVersion = 0;
    }

    @AfterEach
    public void afterEach() {
        ConnectionInfo.protocolVersion = SharedConstants.getGameVersion().getProtocolVersion();
    }

    @Test
    public void testUntransformedRead() {
        TransformerByteBuf buf = buf(NetworkSide.CLIENTBOUND, LoginHelloS2CPacket.class,
                new TranslatorRegistry(),
                0, 1, 2, 3, 4, 5, 6, 7, 0xc0, 0x84, 0x3d);
        assertEquals(0, buf.readVarInt());
        assertEquals(1, buf.readByte());
        assertEquals(0x203, buf.readShort());
        assertEquals(0x4050607, buf.readInt());
        assertEquals(1000000, buf.readVarInt());
    }

    @Test
    public void testConsumedRead() {
        TransformerByteBuf buf = buf(NetworkSide.CLIENTBOUND, LoginHelloS2CPacket.class,
                new TranslatorRegistry()
                    .registerInboundTranslator(0, LoginHelloS2CPacket.class, TransformerByteBuf::readByte),
                0, 1, 2);
        assertEquals(0, buf.readVarInt());
        assertEquals(2, buf.readByte());
    }

    @SuppressWarnings("unchecked")
    private static TransformerByteBuf buf(NetworkSide direction, Class<? extends Packet<?>> packetClass, TranslatorRegistry translatorRegistry, int... arr) {
        byte[] bytes = new byte[arr.length];
        for (int i = 0; i < arr.length; i++)
            bytes[i] = (byte) arr[i];
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(arr.length);
        buf.writeBytes(bytes);

        ChannelHandlerContext context = mock(ChannelHandlerContext.class);
        Channel channel = mock(Channel.class);
        Attribute<NetworkState> attr = mock(Attribute.class);
        NetworkState state = mock(NetworkState.class);
        Map<NetworkSide, BiMap<Integer, Class<? extends Packet<?>>>> packetMap = ImmutableMap.of(direction, ImmutableBiMap.of(arr[0], packetClass));
        when(context.channel()).thenReturn(channel);
        when(channel.attr(ClientConnection.ATTR_KEY_PROTOCOL)).thenReturn(attr);
        when(attr.get()).thenReturn(state);
        when(((INetworkState) state).getPacketHandlerMap()).thenReturn(packetMap);

        return new TransformerByteBuf(buf, context, translatorRegistry);
    }

}
