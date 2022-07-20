package net.earthcomputer.multiconnect.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.earthcomputer.multiconnect.debug.PacketReplay;
import net.earthcomputer.multiconnect.mixin.connect.ConnectionAccessor;
import net.earthcomputer.multiconnect.protocols.generic.TypedMap;
import net.minecraft.SharedConstants;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
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

    @Contract("null, _, _, _ -> null; !null, _, _, _ -> !null")
    @Nullable
    public static CompoundTag datafix(@Nullable CompoundTag data, DataFixer fixer, DSL.TypeReference type, int fromVersion) {
        if (data == null) {
            return null;
        }

        return (CompoundTag) fixer.update(
                type,
                new Dynamic<>(NbtOps.INSTANCE, data),
                fromVersion,
                SharedConstants.getCurrentVersion().getDataVersion().getVersion()
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

    @SuppressWarnings("unchecked")
    public static <V> Int2ObjectMap<V> makeInt2ObjectMap(Object... kvs) {
        var map = new Int2ObjectOpenHashMap<V>();
        for (int i = 0; i < kvs.length; i += 2) {
            map.put(((Integer) kvs[i]).intValue(), (V) kvs[i + 1]);
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    public static <K> Object2IntMap<K> makeObject2IntMap(Object... kvs) {
        var map = new Object2IntOpenHashMap<K>();
        for (int i = 0; i < kvs.length; i += 2) {
            map.put((K) kvs[i], ((Integer) kvs[i + 1]).intValue());
        }
        return map;
    }

    /**
     * Decodes a bitset from a string. The string is
     * <a href="https://en.wikipedia.org/wiki/Run-length_encoding">run-length encoded</a>, in the format:
     * {@code (<length>)*}.
     *
     * Since the only possible values in the bitset are true and false, there is no need to explicitly
     * encode the value in this string, as they will always alternative between 0 and 1. The first length
     * in the string refers to the number of 0s, followed by the second length specifying the number of 1s
     * following those 0s, and so on.
     *
     * The lengths are "varchars". More chars are continuously read from the string until one of the chars
     * has its highest bit (0x8000) unset. The bottom 15 bits of each char are included in the length, in
     * little-endian order.
     */
    public static BitSet makeRLEBitSet(String encoded) {
        BitSet result = new BitSet();
        int resultIndex = 0;
        int strIndex = 0;
        boolean value = false;
        while (strIndex < encoded.length()) {
            int length = 0;
            int shift = 0;
            char c;
            do {
                c = encoded.charAt(strIndex++);
                length |= (c & 0x7fff) << shift;
                shift += 15;
            } while ((c & 0x8000) != 0);

            if (value) {
                result.set(resultIndex, resultIndex + length);
            }
            resultIndex += length;
            value = !value;
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Comparable<T>> Set<BlockState> makeMulticonnectBlockStateSet(String... strings) {
        var ret = ImmutableSet.<BlockState>builder();
        List<String> invalidStates = null;
        for (String string : strings) {
            try {
                int bracketIndex = string.indexOf('[');
                String blockName;
                if (bracketIndex == -1) {
                    blockName = string;
                } else {
                    blockName = string.substring(0, bracketIndex);
                }
                Block block = Registry.BLOCK.getOrThrow(ResourceKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation("multiconnect", blockName)));
                if (bracketIndex == -1) {
                    ret.add(block.defaultBlockState());
                } else {
                    String stateString = string.substring(bracketIndex + 1, string.length() - 1);
                    BlockState state = block.defaultBlockState();
                    StateDefinition<Block, BlockState> stateManager = block.getStateDefinition();
                    for (String propertyString : stateString.split(",")) {
                        String[] propertySplit = propertyString.split("=", 2);
                        String propertyName = propertySplit[0];
                        String propertyValue = propertySplit[1];
                        Property<T> property = (Property<T>) stateManager.getProperty(propertyName);
                        Objects.requireNonNull(property);
                        state = state.setValue(property, property.getValue(propertyValue).orElseThrow(() -> new IllegalArgumentException("Could not parse property " + propertyName + " with value " + propertyValue)));
                    }
                    ret.add(state);
                }
            } catch (Throwable e) {
                if (invalidStates == null) {
                    invalidStates = new ArrayList<>();
                }
                invalidStates.add(string);
            }
        }
        if (invalidStates != null) {
            throw new IllegalArgumentException("Could not parse multiconnect block states: " + invalidStates);
        }
        return ret.build();
    }

    public static int getStateId(ResourceKey<Block> blockKey, int offset) {
        Block block = Registry.BLOCK.get(blockKey);
        if (block == null) {
            throw new AssertionError("Could not find block " + blockKey.location());
        }
        BlockState firstState = block.getStateDefinition().getPossibleStates().get(0);
        return Block.getId(firstState) + offset;
    }

    public static void sendRawToServer(ClientPacketListener connection, List<ByteBuf> bufs) {
        if (bufs.isEmpty()) {
            return;
        }

        ChannelPipeline pipeline = ((ConnectionAccessor) connection.getConnection()).getChannel().pipeline();
        ChannelHandlerContext context = pipeline.context("multiconnect_serverbound_translator");
        if (context == null) {
            // maybe we're in a different ConnectionProtocol
            context = pipeline.context("encoder");
            if (context == null) {
                // probably singleplayer
                for (ByteBuf buf : bufs) {
                    Packet<?> packet = decodeVanillaPacket(buf, ConnectionProtocol.PLAY, PacketFlow.SERVERBOUND);
                    if (packet != null) {
                        pipeline.write(packet);
                    }
                }
                pipeline.flush();
                return;
            }
        }

        for (ByteBuf buf : bufs) {
            // don't need to set the user data here, it's not accessed again
            context.write(buf);
        }
        context.flush();
    }

    public static void sendRawToClient(ClientPacketListener connection, TypedMap userData, List<ByteBuf> bufs) {
        if (bufs.isEmpty()) {
            return;
        }

        ChannelPipeline pipeline = ((ConnectionAccessor) connection.getConnection()).getChannel().pipeline();
        ChannelHandlerContext context = pipeline.context("multiconnect_clientbound_translator");
        if (context == null) {
            // maybe we're in a different ConnectionProtocol
            List<String> names = pipeline.names();
            int decoderIndex = names.indexOf("decoder");
            if (decoderIndex < 0) {
                // probably singleplayer
                for (ByteBuf buf : bufs) {
                    Packet<?> packet = decodeVanillaPacket(buf, ConnectionProtocol.PLAY, PacketFlow.CLIENTBOUND);
                    if (packet != null) {
                        pipeline.fireChannelRead(packet);
                    }
                }
                pipeline.flush();
                return;
            }
            if (decoderIndex > 0) {
                context = pipeline.context(names.get(decoderIndex - 1));
            }
        }

        for (ByteBuf buf : bufs) {
            PacketSystem.Internals.setUserData(buf, userData);
            if (context == null) {
                pipeline.fireChannelRead(buf);
            } else {
                context.fireChannelRead(buf);
            }
        }
    }

    public static void onPacketDeserialized(Object packet, boolean clientbound) {
        PacketReplay.onPacketDeserialized(packet, clientbound);
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

    public static String readString(ByteBuf buf, int maxLength) {
        int length = readVarInt(buf);
        if (length > maxLength * 3) {
            throw new DecoderException("The received encoded string buffer length is longer than maximum allowed (" + length + " > " + maxLength * 3 + ")");
        } else if (length < 0) {
            throw new DecoderException("The received encoded string buffer length is less than zero! Weird string!");
        }
        String string = buf.toString(buf.readerIndex(), length, StandardCharsets.UTF_8);
        buf.readerIndex(buf.readerIndex() + length);
        if (string.length() > maxLength) {
            throw new DecoderException("The received string length is longer than maximum allowed (" + length + " > " + maxLength + ")");
        }
        return string;
    }

    public static CompoundTag readNbtCompound(ByteBuf buf) {
        int index = buf.readerIndex();
        byte b = buf.readByte();
        if (b == 0) {
            return null;
        }
        buf.readerIndex(index);
        try {
            return NbtIo.read(new ByteBufInputStream(buf), new NbtAccounter(2097152));
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

    public static void writeNbtCompound(ByteBuf buf, @Nullable CompoundTag value) {
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

    @Nullable
    public static Packet<?> decodeVanillaPacket(ByteBuf buf_, ConnectionProtocol protocol, PacketFlow flow) {
        FriendlyByteBuf buf = new FriendlyByteBuf(buf_);
        int packetId = buf.readVarInt();
        return protocol.createPacket(flow, packetId, buf);
    }

    @FunctionalInterface
    public interface PacketSender {
        void send(
                Object packet,
                List<ByteBuf> outBufs,
                ClientPacketListener connection,
                Map<Class<?>, Object> globalData,
                TypedMap userData
        );
    }

    @FunctionalInterface
    public interface RawPacketSender {
        void send(
                ByteBuf packet,
                List<ByteBuf> outBufs,
                ClientPacketListener connection,
                Map<Class<?>, Object> globalData,
                TypedMap userData
        );
    }

    public record StartSendPacketResult(Class<?>[] readDependencies, Class<?>[] writeDependencies, RawPacketSender sender) {}
}
