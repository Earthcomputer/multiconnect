package net.earthcomputer.multiconnect.impl;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import net.earthcomputer.multiconnect.mixin.connect.ClientConnectionAccessor;
import net.earthcomputer.multiconnect.protocols.generic.TypedMap;
import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtTagSizeTracker;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.IntUnaryOperator;
import java.util.function.LongUnaryOperator;

public final class PacketIntrinsics {
    private PacketIntrinsics() {}

    @SuppressWarnings("unchecked")
    public static <T extends Throwable> RuntimeException sneakyThrow(Throwable ex) throws T {
        throw (T) ex;
    }

    public static MethodHandle findSetterHandle(Class<?> ownerClass, String fieldName, Class<?> fieldType) {
        try {
            return MethodHandles.lookup().findSetter(ownerClass, fieldName, fieldType);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Could not find setter method handle, this indicates a compiler bug!", e);
        }
    }

    public static NbtCompound datafix(NbtCompound data, DataFixer fixer, DSL.TypeReference type, int fromVersion) {
        return (NbtCompound) fixer.update(
                type,
                new Dynamic<>(NbtOps.INSTANCE, data),
                fromVersion,
                SharedConstants.getGameVersion().getWorldVersion()
        ).getValue();
    }

    public static OptionalInt map(OptionalInt value, IntUnaryOperator mapper) {
        if (value.isPresent()) {
            return OptionalInt.of(mapper.applyAsInt(value.getAsInt()));
        } else {
            return OptionalInt.empty();
        }
    }

    public static OptionalLong map(OptionalLong value, LongUnaryOperator mapper) {
        if (value.isPresent()) {
            return OptionalLong.of(mapper.applyAsLong(value.getAsLong()));
        } else {
            return OptionalLong.empty();
        }
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> makeMap(Object... kvs) {
        var builder = ImmutableMap.<K, V>builder();
        for (int i = 0; i < kvs.length; i += 2) {
            builder.put((K) kvs[i], (V) kvs[i + 1]);
        }
        return builder.build();
    }

    public static int getStateId(RegistryKey<Block> blockKey, int offset) {
        Block block = Registry.BLOCK.get(blockKey);
        assert block != null;
        BlockState firstState = block.getStateManager().getStates().get(0);
        return Block.getRawIdFromState(firstState) + offset;
    }

    public static void sendRawToServer(ClientPlayNetworkHandler networkHandler, List<ByteBuf> bufs) {
        if (bufs.isEmpty()) {
            return;
        }

        ChannelHandlerContext context = ((ClientConnectionAccessor) networkHandler.getConnection()).getChannel()
                .pipeline()
                .context("multiconnect_serverbound_translator");

        for (ByteBuf buf : bufs) {
            // don't need to set the user data here, it's not accessed again
            context.write(buf);
        }
        context.flush();
    }

    public static void sendRawToClient(ClientPlayNetworkHandler networkHandler, TypedMap userData, List<ByteBuf> bufs) {
        if (bufs.isEmpty()) {
            return;
        }

        ChannelHandlerContext context = ((ClientConnectionAccessor) networkHandler.getConnection()).getChannel()
                .pipeline()
                .context("multiconnect_clientbound_translator");

        for (ByteBuf buf : bufs) {
            PacketSystem.Internals.setUserData(buf, userData);
            context.fireChannelRead(buf);
        }
    }

    public static int readVarInt(ByteBuf buf) {
        int result = 0;
        int shift = 0;
        int b;
        do {
            if (shift >= 32) {
                throw new IndexOutOfBoundsException("varint too big");
            }
            b = buf.readUnsignedByte();
            result |= (b & 0x7f) << shift;
            shift += 7;
        } while ((b & 0x80) != 0);
        return result;
    }

    public static long readVarLong(ByteBuf buf) {
        long result = 0;
        int shift = 0;
        int b;
        do {
            if (shift >= 64) {
                throw new IndexOutOfBoundsException("varlong too big");
            }
            b = buf.readUnsignedByte();
            result |= (long) (b & 0x7f) << shift;
            shift += 7;
        } while ((b & 0x80) != 0);
        return result;
    }

    public static String readString(ByteBuf buf) {
        int length = readVarInt(buf);
        if (length > PacketByteBuf.DEFAULT_MAX_STRING_LENGTH * 4) {
            throw new DecoderException("The received encoded string buffer length is longer than maximum allowed (" + length + " > " + PacketByteBuf.DEFAULT_MAX_STRING_LENGTH * 4 + ")");
        } else if (length < 0) {
            throw new DecoderException("The received encoded string buffer length is less than zero! Weird string!");
        }
        String string = buf.toString(buf.readerIndex(), length, StandardCharsets.UTF_8);
        buf.readerIndex(buf.readerIndex() + length);
        if (string.length() > PacketByteBuf.DEFAULT_MAX_STRING_LENGTH) {
            throw new DecoderException("The received string length is longer than maximum allowed (" + length + " > " + PacketByteBuf.DEFAULT_MAX_STRING_LENGTH + ")");
        }
        return string;
    }

    public static NbtCompound readNbtCompound(ByteBuf buf) {
        int index = buf.readerIndex();
        byte b = buf.readByte();
        if (b == 0) {
            return null;
        }
        buf.readerIndex(index);
        try {
            return NbtIo.read(new ByteBufInputStream(buf), new NbtTagSizeTracker(2097152));
        } catch (IOException e) {
            throw new DecoderException(e);
        }
    }

    public static BitSet readBitSet(ByteBuf buf) {
        int length = readVarInt(buf);
        if (length > buf.readableBytes() / 8) {
            throw new DecoderException("LongArray with size " + length + " is bigger than allowed " + buf.readableBytes() / 8);
        }
        long[] array = new long[length];
        for (int i = 0; i < length; i++) {
            array[i] = buf.readLong();
        }
        return BitSet.valueOf(array);
    }

    public static void writeVarInt(ByteBuf buf, int value) {
        do {
            int bits = value & 0x7f;
            value >>>= 7;
            buf.writeByte(bits | ((value != 0) ? 0x80 : 0));
        } while (value != 0);
    }

    public static void writeVarLong(ByteBuf buf, long value) {
        do {
            int bits = (int) (value & 0x7f);
            value >>>= 7;
            buf.writeByte(bits | ((value != 0) ? 0x80 : 0));
        } while (value != 0);
    }

    public static void writeString(ByteBuf buf, String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        writeVarInt(buf, bytes.length);
        buf.writeBytes(bytes);
    }

    public static void writeNbtCompound(ByteBuf buf, NbtCompound value) {
        if (value == null) {
            buf.writeByte(0);
        } else {
            try {
                NbtIo.write(value, new ByteBufOutputStream(buf));
            } catch (IOException e) {
                throw new EncoderException(e);
            }
        }
    }

    public static void writeBitSet(ByteBuf buf, BitSet value) {
        long[] longs = value.toLongArray();
        writeVarInt(buf, longs.length);
        for (long element : longs) {
            buf.writeLong(element);
        }
    }

    @FunctionalInterface
    public interface PacketSender {
        void send(Object packet, List<ByteBuf> outBufs, ClientPlayNetworkHandler networkHandler, TypedMap userData);
    }
}
