package net.earthcomputer.multiconnect.transformer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.SharedConstants;
import net.minecraft.client.network.packet.LoginHelloS2CPacket;
import net.minecraft.network.Packet;
import net.minecraft.server.network.packet.LoginHelloC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

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
        TransformerByteBuf buf = inboundBuf(new TranslatorRegistry(),
                1, 2, 3, 4, 5, 6, 7, 0xc0, 0x84, 0x3d);
        assertEquals(1, buf.readByte());
        assertEquals(0x203, buf.readShort());
        assertEquals(0x4050607, buf.readInt());
        assertEquals(1000000, buf.readVarInt());
    }

    @Test
    public void testConsumedRead() {
        TransformerByteBuf buf = inboundBuf(new TranslatorRegistry()
                    .registerInboundTranslator(0, LoginHelloS2CPacket.class, TransformerByteBuf::readByte),
                1, 2);
        assertEquals(2, buf.readByte());
    }

    @Test
    public void testUnconsumedRead() {
        TransformerByteBuf buf = inboundBuf(new TranslatorRegistry()
                    .registerInboundTranslator(-1, LoginHelloS2CPacket.class, TransformerByteBuf::readByte),
                1, 2);
        assertEquals(1, buf.readByte());
        assertEquals(2, buf.readByte());
    }

    @Test
    public void testPassthroughWithNestedTranslator() {
        TransformerByteBuf buf = inboundBuf(new TranslatorRegistry()
                    .registerInboundTranslator(1, LoginHelloS2CPacket.class, buf1 -> {
                        buf1.enablePassthroughMode();
                        buf1.readVarInt();
                        buf1.readVarInt();
                        buf1.readVarInt();
                        buf1.disablePassthroughMode();
                        buf1.applyPendingReads();
                    })
                    .registerInboundTranslator(0, VarInt.class, buf1 -> {
                        buf1.pendingRead(Byte.class, (byte) (buf1.readByte() & 0x7f));
                        buf1.applyPendingReads();
                    }),
                    0xc0, 0x84, 0x3d);
        assertEquals(0xc0 & 0x7f, buf.readVarInt());
        assertEquals(0x84 & 0x7f, buf.readVarInt());
        assertEquals(0x3d, buf.readVarInt());
    }

    @Test
    public void testPendingRead() {
        TransformerByteBuf buf = inboundBuf(new TranslatorRegistry()
                    .registerInboundTranslator(0, LoginHelloS2CPacket.class, buf1 -> {
                        buf1.pendingRead(Byte.class, (byte)3);
                        buf1.applyPendingReads();
                    }),
                1, 2);
        assertEquals(3, buf.readByte());
        assertEquals(1, buf.readByte());
        assertEquals(2, buf.readByte());
    }

    @Test
    public void testPassthroughRead() {
        TransformerByteBuf buf = inboundBuf(new TranslatorRegistry()
                    .registerInboundTranslator(0, LoginHelloS2CPacket.class, buf1 -> {
                        buf1.enablePassthroughMode();
                        buf1.readByte();
                        buf1.disablePassthroughMode();
                        buf1.applyPendingReads();
                    }),
                1, 2);
        assertEquals(1, buf.readByte());
        assertEquals(2, buf.readByte());
    }

    @Test
    public void testNestedRead() {
        TransformerByteBuf buf = inboundBuf(new TranslatorRegistry()
                    .registerInboundTranslator(0, VarInt.class, buf1 -> {
                        buf1.pendingRead(Byte.class, (byte)(buf1.readByte() & 0x7f));
                        buf1.applyPendingReads();
                    }),
                0xc0, 0x84, 0x3d);
        assertEquals(0xc0 & 0x7f, buf.readVarInt());
        assertEquals(0x84 & 0x7f, buf.readVarInt());
        assertEquals(0x3d, buf.readVarInt());
    }

    @Test
    public void testChainedRead() {
        TransformerByteBuf buf = inboundBuf(new TranslatorRegistry()
                    .registerInboundTranslator(2, LoginHelloS2CPacket.class, buf1 -> {
                        buf1.pendingRead(VarInt.class, new VarInt(Integer.parseInt(buf1.readString())));
                        buf1.applyPendingReads();
                    })
                    .registerInboundTranslator(1, LoginHelloS2CPacket.class, buf1 -> {
                        buf1.pendingRead(String.class, new StringBuilder(buf1.readString()).reverse().toString());
                        buf1.applyPendingReads();
                    })
                    .registerInboundTranslator(0, LoginHelloS2CPacket.class, buf1 -> {
                        buf1.pendingRead(String.class, String.valueOf(buf1.readVarInt()));
                        buf1.applyPendingReads();
                    }),
                0xd2, 0x09);
        assertEquals(4321, buf.readVarInt());
    }

    @Test
    public void testSimpleTranslateRead() {
        TransformerByteBuf buf = inboundBuf(new TranslatorRegistry()
                    .registerInboundTranslator(0, Integer.class, new InboundTranslator<Integer>() {
                        @Override
                        public void onRead(TransformerByteBuf buf) {
                        }

                        @Override
                        public Integer translate(Integer from) {
                            return from + 47;
                        }
                    }),
                0, 0, 0, 53);
        assertEquals(100, buf.readInt());
    }

    @Test
    public void testInapplicableTranslateRead() {
        TransformerByteBuf buf = inboundBuf(new TranslatorRegistry()
                    .registerInboundTranslator(-1, Integer.class, new InboundTranslator<Integer>() {
                        @Override
                        public void onRead(TransformerByteBuf buf) {
                        }

                        @Override
                        public Integer translate(Integer from) {
                            return from + 47;
                        }
                    }),
                    0, 0, 0, 53);
        assertEquals(53, buf.readInt());
    }

    @Test
    public void testUntransformedWrite() {
        TransformerByteBuf buf = outboundBuf(new TranslatorRegistry(), 10);
        buf.writeByte(1);
        buf.writeShort(0x203);
        buf.writeInt(0x4050607);
        buf.writeVarInt(1000000);
        assertWrittenEquals(buf, 1, 2, 3, 4, 5, 6, 7, 0xc0, 0x84, 0x3d);
    }

    @Test
    public void testSkippedWrite() {
        TransformerByteBuf buf = outboundBuf(new TranslatorRegistry()
                    .registerOutboundTranslator(0, LoginHelloC2SPacket.class, buf1 -> buf1.skipWrite(Byte.class)),
                    1);
        buf.writeByte(1); // skipped
        buf.writeByte(2);
        assertWrittenEquals(buf, 2);
    }

    @Test
    public void testUnskippedWrite() {
        TransformerByteBuf buf = outboundBuf(new TranslatorRegistry()
                    .registerOutboundTranslator(-1, LoginHelloC2SPacket.class, buf1 -> buf1.skipWrite(Byte.class)),
                    2);
        buf.writeByte(1);
        buf.writeByte(2);
        assertWrittenEquals(buf, 1, 2);
    }

    @Test
    public void testPendingWrite() {
        TransformerByteBuf buf = outboundBuf(new TranslatorRegistry()
                    .registerOutboundTranslator(0, LoginHelloC2SPacket.class, buf1 -> {
                        Supplier<Byte> val = buf1.passthroughWrite(Byte.class);
                        buf1.pendingWrite(Byte.class, () -> (byte) (val.get() + 47), (Consumer<Byte>) buf1::writeByte);
                    }),
                    3);
        buf.writeByte(53);
        buf.writeByte(0xcc);
        assertWrittenEquals(buf, 53, 100, 0xcc);
    }

    @Test
    public void testNestedWrite() {
        TransformerByteBuf buf = outboundBuf(new TranslatorRegistry()
                    .registerOutboundTranslator(0, VarInt.class, buf1 -> {
                        Supplier<Byte> val = buf1.skipWrite(Byte.class);
                        buf1.pendingWrite(Byte.class, () -> (byte) (val.get() & 0x7f), (Consumer<Byte>) buf1::writeByte);
                    }),
                    4);
        buf.writeByte(47);
        buf.writeVarInt(1000000);
        assertWrittenEquals(buf, 47, 0xc0 & 0x7f, 0x84, 0x3d);
    }

    @Test
    public void testInapplicableNewTranslatorWrite() {
        TransformerByteBuf buf = outboundBuf(new TranslatorRegistry()
                    .registerOutboundTranslator(1, VarInt.class, buf1 -> {
                        Supplier<Byte> val = buf1.skipWrite(Byte.class);
                        buf1.pendingWrite(Byte.class, () -> (byte) (val.get() & 0x7f), (Consumer<Byte>) buf1::writeByte);
                    })
                    .registerOutboundTranslator(0, LoginHelloC2SPacket.class, buf1 -> {
                        Supplier<VarInt> val = buf1.skipWrite(VarInt.class);
                        buf1.pendingWrite(Integer.class, () -> val.get().get(), buf1::writeInt);
                    }),
                    4);
        buf.writeVarInt(1000000);
        assertWrittenEquals(buf, 0, 0xf, 0x42, 0x40);
    }

    @Test
    public void testChainedWrite() {
        TransformerByteBuf buf = outboundBuf(new TranslatorRegistry()
                    .registerOutboundTranslator(2, LoginHelloC2SPacket.class, buf1 -> {
                        Supplier<VarInt> val = buf1.skipWrite(VarInt.class);
                        buf1.pendingWrite(String.class, () -> String.valueOf(val.get().get()), buf1::writeString);
                    })
                    .registerOutboundTranslator(1, LoginHelloC2SPacket.class, buf1 -> {
                        Supplier<String> val = buf1.skipWrite(String.class);
                        buf1.pendingWrite(String.class, () -> new StringBuilder(val.get()).reverse().toString(), buf1::writeString);
                    })
                    .registerOutboundTranslator(0, LoginHelloC2SPacket.class, buf1 -> {
                        Supplier<String> val = buf1.skipWrite(String.class);
                        buf1.pendingWrite(VarInt.class, () -> new VarInt(Integer.parseInt(val.get())), val1 -> buf1.writeVarInt(val1.get()));
                    }),
                    3);
        buf.writeVarInt(4321);
        assertWrittenEquals(buf, 0xd2, 0x09);
    }

    @Test
    public void testSimpleTranslateWrite() {
        TransformerByteBuf buf = outboundBuf(new TranslatorRegistry()
                    .registerOutboundTranslator(0, Integer.class, new OutboundTranslator<Integer>() {
                        @Override
                        public void onWrite(TransformerByteBuf buf) {
                        }

                        @Override
                        public Integer translate(Integer from) {
                            return from + 47;
                        }
                    }),
                    4);
        buf.writeInt(53);
        assertWrittenEquals(buf, 0, 0, 0, 100);
    }

    @Test
    public void testInapplicableTranslateWrite() {
        TransformerByteBuf buf = outboundBuf(new TranslatorRegistry()
                        .registerOutboundTranslator(-1, Integer.class, new OutboundTranslator<Integer>() {
                            @Override
                            public void onWrite(TransformerByteBuf buf) {
                            }

                            @Override
                            public Integer translate(Integer from) {
                                return from + 47;
                            }
                        }),
                4);
        buf.writeInt(53);
        assertWrittenEquals(buf, 0, 0, 0, 53);
    }

    @Test
    public void testMultiplePendingWrite() {
        TransformerByteBuf buf = outboundBuf(new TranslatorRegistry()
                    .registerOutboundTranslator(1, LoginHelloC2SPacket.class, buf1 -> {
                        Supplier<Hand> hand = buf1.skipWrite(Hand.class);
                        Supplier<BlockHitResult> hitResult = buf1.skipWrite(BlockHitResult.class);

                        buf1.pendingWrite(BlockPos.class, () -> hitResult.get().getBlockPos(), buf1::writeBlockPos);
                        buf1.pendingWrite(Direction.class, () -> hitResult.get().getSide(), buf1::writeEnumConstant);
                        buf1.pendingWrite(Hand.class, hand, buf1::writeEnumConstant);
                        buf1.pendingWrite(Float.class, () -> (float) (hitResult.get().getPos().x - hitResult.get().getBlockPos().getX()), buf1::writeFloat);
                        buf1.pendingWrite(Float.class, () -> (float) (hitResult.get().getPos().y - hitResult.get().getBlockPos().getY()), buf1::writeFloat);
                        buf1.pendingWrite(Float.class, () -> (float) (hitResult.get().getPos().z - hitResult.get().getBlockPos().getZ()), buf1::writeFloat);
                    })
                    .registerOutboundTranslator(1, BlockPos.class, buf1 -> {
                        Supplier<Long> val = buf1.skipWrite(Long.class);
                        buf1.pendingWrite(Long.class, () -> {
                            BlockPos pos = BlockPos.fromLong(val.get());
                            pos = new BlockPos(pos.getZ(), pos.getY(), pos.getX());
                            return pos.asLong();
                        }, buf1::writeLong);
                    }),
                    22);
        buf.writeEnumConstant(Hand.OFF_HAND);
        buf.writeBlockHitResult(new BlockHitResult(new Vec3d(2, 3, 5), Direction.WEST, new BlockPos(7, 11, 13), false));

        assertEquals(new BlockPos(13, 11, 7), buf.readBlockPos());
        assertEquals(Direction.WEST, buf.readEnumConstant(Direction.class));
        assertEquals(Hand.OFF_HAND, buf.readEnumConstant(Hand.class));
    }

    private static TransformerByteBuf inboundBuf(TranslatorRegistry registry, int... arr) {
        return inboundBuf(LoginHelloS2CPacket.class, registry, arr);
    }

    private static TransformerByteBuf outboundBuf(TranslatorRegistry registry, int capacity) {
        return outboundBuf(LoginHelloC2SPacket.class, registry, capacity);
    }

    public static TransformerByteBuf inboundBuf(Class<? extends Packet<?>> packetClass,
                                                TranslatorRegistry registry,
                                                int... arr) {
        byte[] bytes = new byte[arr.length];
        for (int i = 0; i < arr.length; i++)
            bytes[i] = (byte) arr[i];
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(arr.length);
        buf.writeBytes(bytes);
        TransformerByteBuf transformerBuf = new TransformerByteBuf(buf, null, registry);
        transformerBuf.readTopLevelType(packetClass);
        return transformerBuf;
    }

    public static TransformerByteBuf outboundBuf(Class<? extends Packet<?>> packetClass,
                                                 TranslatorRegistry registry,
                                                 int capacity) {
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(capacity, capacity);
        TransformerByteBuf transformerBuf = new TransformerByteBuf(buf, null, registry);
        transformerBuf.writeTopLevelType(packetClass);
        return transformerBuf;
    }

    public static void assertWrittenEquals(ByteBuf actual, int... expected) {
        assertEquals(expected.length, actual.readableBytes(), "Wrong number of bytes written");
        byte[] actualBytes = new byte[expected.length];
        actual.readBytes(actualBytes);
        for (int i = 0; i < expected.length; i++)
            assertEquals((byte) expected[i], actualBytes[i], "At index " + i);
    }

}
