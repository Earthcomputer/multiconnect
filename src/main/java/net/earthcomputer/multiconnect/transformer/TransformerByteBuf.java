package net.earthcomputer.multiconnect.transformer;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import it.unimi.dsi.fastutil.ints.IntList;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.generic.INetworkState;
import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.minecraft.SharedConstants;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.*;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.service.MixinService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.*;

public final class TransformerByteBuf extends PacketByteBuf {
    private static final Logger LOGGER = LogManager.getLogger("multiconnect");

    private final ChannelHandlerContext context;
    private final TranslatorRegistry translatorRegistry;

    private boolean transformationEnabled = false;
    private final Deque<StackFrame> stack = new ArrayDeque<>();
    private boolean forceSuper = false;

    public TransformerByteBuf(ByteBuf delegate, ChannelHandlerContext context) {
        this(delegate, context, ProtocolRegistry.getTranslatorRegistry());
    }

    public TransformerByteBuf(ByteBuf delegate, ChannelHandlerContext context, TranslatorRegistry translatorRegistry) {
        super(delegate);
        this.context = context;
        this.translatorRegistry = translatorRegistry;
    }

    public ClientConnection getConnection() {
        if (context == null) {
            return null;
        }
        return context.channel().pipeline().get(ClientConnection.class);
    }

    public TransformerByteBuf readTopLevelType(Class<?> type) {
        return readTopLevelType(type, ConnectionInfo.protocolVersion, buf -> {});
    }

    @SuppressWarnings("unchecked")
    public <T> TransformerByteBuf readTopLevelType(Class<T> type, int protocolVersion, InboundTranslator<T> additionalTranslator) {
        transformationEnabled = true;
        stack.push(new StackFrame(type, protocolVersion));
        additionalTranslator.onRead(this);
        List<Pair<Integer, InboundTranslator<?>>> translators = (List<Pair<Integer, InboundTranslator<?>>>) (List<?>)
                translatorRegistry.getInboundTranslators(type, protocolVersion, SharedConstants.getGameVersion().getProtocolVersion());
        for (Pair<Integer, InboundTranslator<?>> translator : translators) {
            getStackFrame().version = translator.getLeft();
            translator.getRight().onRead(this);
        }
        getStackFrame().version = SharedConstants.getGameVersion().getProtocolVersion();
        return this;
    }

    @SuppressWarnings("unchecked")
    public TransformerByteBuf writeTopLevelType(Class<?> type) {
        transformationEnabled = true;
        stack.push(new StackFrame(type, SharedConstants.getGameVersion().getProtocolVersion()));
        List<Pair<Integer, OutboundTranslator<?>>> translators = (List<Pair<Integer, OutboundTranslator<?>>>) (List<?>)
                translatorRegistry.getOutboundTranslators(type, ConnectionInfo.protocolVersion, SharedConstants.getGameVersion().getProtocolVersion());
        for (Pair<Integer, OutboundTranslator<?>> translator : translators) {
            translator.getRight().onWrite(this);
            getStackFrame().version = translator.getLeft();
        }
        getStackFrame().version = SharedConstants.getGameVersion().getProtocolVersion();
        return this;
    }

    public void enablePassthroughMode() {
        getStackFrame().passthroughMode = true;
    }

    public void disablePassthroughMode() {
        getStackFrame().passthroughMode = false;
    }

    public void applyPendingReads() {
        getStackFrame().pendingPendingReads.forEach((k, v) -> {
            if (getStackFrame().pendingReads.containsKey(k)) {
                Deque<PendingValue<?>> dest = getStackFrame().pendingReads.get(k);
                Iterator<PendingValue<?>> itr = v.descendingIterator();
                while (itr.hasNext())
                    dest.addFirst(itr.next());
            } else {
                getStackFrame().pendingReads.put(k, v);
            }
        });
        getStackFrame().pendingPendingReads.clear();
    }

    @Override
    public int readVarInt() {
        if (!transformationEnabled) {
            int packetId = super.readVarInt();
            NetworkState state = context.channel().attr(ClientConnection.PROTOCOL_ATTRIBUTE_KEY).get();
            //noinspection ConstantConditions
            Class<? extends Packet<?>> packetClass = ((INetworkState) (Object) state).getPacketHandlers()
                    .get(NetworkSide.CLIENTBOUND).multiconnect_getPacketClassById(packetId);
            readTopLevelType(packetClass);
            return packetId;
        } else {
            return read(VarInt.class, () -> new VarInt(super.readVarInt())).get();
        }
    }

    private <T> T read(Class<T> type, Supplier<T> readMethod) {
        return read(type, readMethod, readMethod, value -> {}, Function.identity());
    }

    private <STORED> TransformerByteBuf read(Class<?> type, Supplier<ByteBuf> readMethod, Supplier<STORED> storedValueExtractor, Consumer<STORED> storedValueApplier) {
        return read(type, () -> {
            readMethod.get();
            return this;
        }, storedValueExtractor, storedValueApplier, value -> this);
    }

    @SuppressWarnings("unchecked")
    private <RETURN, STORED> RETURN read(Class<?> type,
                                         Supplier<RETURN> readMethod,
                                         Supplier<STORED> storedValueExtractor,
                                         Consumer<STORED> storedValueApplier,
                                         Function<STORED, RETURN> returnValueExtractor) {
        if (!transformationEnabled)
            return readMethod.get();

        boolean passthroughMode = getStackFrame().passthroughMode;
        Queue<PendingValue<STORED>> pendingReads = (Queue<PendingValue<STORED>>) (Queue<?>) getStackFrame().pendingReads.get(type);
        stack.push(new StackFrame(type, ConnectionInfo.protocolVersion));
        List<Pair<Integer, InboundTranslator<STORED>>> translators;

        STORED value;
        if (pendingReads != null && !pendingReads.isEmpty()) {
            PendingValue<STORED> pendingValue = pendingReads.poll();
            value = pendingValue.value;
            translators = translatorRegistry.getInboundTranslators(type, pendingValue.version, SharedConstants.getGameVersion().getProtocolVersion());
        } else {
            translators = translatorRegistry.getInboundTranslators(type, ConnectionInfo.protocolVersion, SharedConstants.getGameVersion().getProtocolVersion());
            for (Pair<Integer, InboundTranslator<STORED>> translator : translators) {
                getStackFrame().version = translator.getLeft();
                translator.getRight().onRead(this);
            }

            getStackFrame().version = SharedConstants.getGameVersion().getProtocolVersion();
            if (!passthroughMode && translators.isEmpty()) {
                RETURN ret = readMethod.get();
                if (!getStackFrame().pendingPendingReads.isEmpty())
                    throw new IllegalStateException("You forgot to apply pending reads!");
                stack.pop();
                return ret;
            }

            value = storedValueExtractor.get();
        }

        for (Pair<Integer, InboundTranslator<STORED>> translator : translators) {
            getStackFrame().version = translator.getLeft();
            value = translator.getRight().translate(value);
        }

        if (!getStackFrame().pendingPendingReads.isEmpty())
            throw new IllegalStateException("You forgot to apply pending reads!");
        stack.pop();

        if (passthroughMode) {
            pendingReadUnchecked(type, value);
        } else {
            storedValueApplier.accept(value);
        }

        return returnValueExtractor.apply(value);
    }

    // usually you want type and value to be of the same type
    public <T> void pendingRead(Class<T> type, T value) {
        pendingReadUnchecked(type, value);
    }

    public void pendingReadUnchecked(Class<?> type, Object value) {
        Queue<PendingValue<?>> queue = getStackFrame().pendingPendingReads.computeIfAbsent(type, k -> new ArrayDeque<>());
        queue.add(new PendingValue<>(value, getStackFrame().version));
    }

    @Override
    public PacketByteBuf writeVarInt(int val) {
        if (!transformationEnabled) {
            super.writeVarInt(val);
            NetworkState state = context.channel().attr(ClientConnection.PROTOCOL_ATTRIBUTE_KEY).get();
            //noinspection ConstantConditions
            Class<? extends Packet<?>> packetClass = ((INetworkState) (Object) state).getPacketHandlers()
                    .get(NetworkSide.SERVERBOUND).multiconnect_getPacketClassById(val);
            writeTopLevelType(packetClass);
            return this;
        } else {
            return write(VarInt.class, new VarInt(val), v -> super.writeVarInt(v.get()));
        }
    }

    private <T> PacketByteBuf write(Class<T> type, T value, Consumer<T> writeMethod) {
        if (!transformationEnabled || forceSuper) {
            forceSuper = false;
            writeMethod.accept(value);
            return this;
        }

        int version = getStackFrame().version;
        int minVersion = ConnectionInfo.protocolVersion;
        List<Pair<Integer, OutboundTranslator<T>>> translators = translatorRegistry.getOutboundTranslators(type, minVersion, version);

        boolean skipWrite = false;

        int translatorsIndex = 0;
        versionLoop:
        for (Map.Entry<Integer, Queue<WriteInstruction>> entry : getStackFrame().writeInstructions.tailMap(version).entrySet()) {
            int ver = entry.getKey();

            while (translatorsIndex < translators.size() && translators.get(translatorsIndex).getLeft() >= ver) {
                value = translators.get(translatorsIndex).getRight().translate(value);
                translatorsIndex++;
            }
            getStackFrame().version = ver;

            Queue<WriteInstruction> instructions = entry.getValue();
            while (!instructions.isEmpty()) {
                WriteInstruction insn = instructions.poll();
                if (!insn.matchesType(type)) {
                    throw new IllegalStateException("Write instruction expected type " + insn.getExpectedType().getName() + ", but got " + type.getName());
                }
                insn.onWrite(value);
                if (insn.consumesWrite()) {
                    if (insn.skipsWrite()) {
                        skipWrite = true;
                        minVersion = ver;
                        break versionLoop;
                    }
                    break;
                }
            }
        }
        for (; translatorsIndex < translators.size(); translatorsIndex++) {
            value = translators.get(translatorsIndex).getRight().translate(value);
        }
        getStackFrame().version = version;

        if (!skipWrite) {
            stack.push(new StackFrame(type, SharedConstants.getGameVersion().getProtocolVersion()));
            translators = translatorRegistry.getOutboundTranslators(type, minVersion, SharedConstants.getGameVersion().getProtocolVersion());
            for (Pair<Integer, OutboundTranslator<T>> translator : translators) {
                translator.getRight().onWrite(this);
                getStackFrame().version = translator.getLeft();
            }

            getStackFrame().version = SharedConstants.getGameVersion().getProtocolVersion();
            writeMethod.accept(value);

            stack.pop();
        }

        for (Map.Entry<Integer, Queue<WriteInstruction>> entry : getStackFrame().writeInstructions.subMap(version, true, minVersion, true).entrySet()) {
            getStackFrame().version = entry.getKey();
            Queue<WriteInstruction> instructions = entry.getValue();
            while (!instructions.isEmpty() && !instructions.peek().consumesWrite()) {
                WriteInstruction insn = instructions.poll();
                assert insn != null;
                insn.onWrite(null);
            }
        }
        getStackFrame().version = version;

        return this;
    }

    public <T> Supplier<T> skipWrite(Class<T> type) {
        PassthroughWriteInstruction<T> insn = new PassthroughWriteInstruction<>(type, true);
        getStackFrame().writeInstructions.computeIfAbsent(getStackFrame().version, k -> new ArrayDeque<>()).add(insn);
        return () -> insn.value;
    }

    public void whenWrite(Runnable action) {
        WhenWriteInstruction insn = new WhenWriteInstruction(action);
        getStackFrame().writeInstructions.computeIfAbsent(getStackFrame().version, k -> new ArrayDeque<>()).add(insn);
    }

    public <T> void pendingWrite(Class<T> type, Supplier<T> value, Consumer<T> writeFunction) {
        Queue<WriteInstruction> queue = getStackFrame().writeInstructions.computeIfAbsent(getStackFrame().version, k -> new ArrayDeque<>());
        queue.add(new PendingWriteInstruction<>(type, value, writeFunction));
        queue.add(new PassthroughWriteInstruction<>(type, false));
    }

    public <T> Supplier<T> passthroughWrite(Class<T> type) {
        PassthroughWriteInstruction<T> insn = new PassthroughWriteInstruction<>(type, false);
        getStackFrame().writeInstructions.computeIfAbsent(getStackFrame().version, k -> new ArrayDeque<>()).add(insn);
        return () -> insn.value;
    }

    private StackFrame getStackFrame() {
        assert stack.peek() != null;
        return stack.peek();
    }

    private static final class StackFrame {
        final Class<?> type;
        int version;
        final Map<Class<?>, Deque<PendingValue<?>>> pendingReads = new HashMap<>();
        boolean passthroughMode = false;
        final Map<Class<?>, Deque<PendingValue<?>>> pendingPendingReads = new HashMap<>();
        final TreeMap<Integer, Queue<WriteInstruction>> writeInstructions = new TreeMap<>(Comparator.reverseOrder());

        public StackFrame(Class<?> type, int version) {
            this.type = type;
            this.version = version;
        }
    }

    private static final class PendingValue<STORED> {
        final STORED value;
        final int version;

        public PendingValue(STORED value, int version) {
            this.value = value;
            this.version = version;
        }
    }

    private interface WriteInstruction {
        boolean matchesType(Class<?> type);

        default Class<?> getExpectedType() {
            return null;
        }

        boolean consumesWrite();

        boolean skipsWrite();

        void onWrite(Object value);
    }

    private static final class PassthroughWriteInstruction<T> implements WriteInstruction {
        final Class<T> type;
        final boolean skip;
        T value;

        PassthroughWriteInstruction(Class<T> type, boolean skip) {
            this.type = type;
            this.skip = skip;
        }

        @Override
        public boolean matchesType(Class<?> type) {
            return type == this.type;
        }

        @Override
        public Class<?> getExpectedType() {
            return this.type;
        }

        @Override
        public boolean consumesWrite() {
            return true;
        }

        @Override
        public boolean skipsWrite() {
            return skip;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void onWrite(Object value) {
            this.value = (T) value;
        }
    }

    private static final class WhenWriteInstruction implements WriteInstruction {
        final Runnable action;

        WhenWriteInstruction(Runnable action) {
            this.action = action;
        }

        @Override
        public boolean matchesType(Class<?> type) {
            return true;
        }

        @Override
        public boolean consumesWrite() {
            return false;
        }

        @Override
        public boolean skipsWrite() {
            return false;
        }

        @Override
        public void onWrite(Object value) {
            action.run();
        }
    }

    private final class PendingWriteInstruction<T> implements WriteInstruction {
        final Class<T> type;
        final Supplier<T> value;
        final Consumer<T> writeMethod;

        PendingWriteInstruction(Class<T> type, Supplier<T> value, Consumer<T> writeMethod) {
            this.type = type;
            this.value = value;
            this.writeMethod = writeMethod;
        }

        @Override
        public boolean matchesType(Class<?> type) {
            return true; // may not be the same type
        }

        @Override
        public boolean consumesWrite() {
            return false;
        }

        @Override
        public boolean skipsWrite() {
            return false;
        }

        @Override
        public void onWrite(Object value) {
            write(type, this.value.get(), v -> {
                forceSuper = true;
                writeMethod.accept(v);
            });
        }
    }

    @Override
    public byte[] readByteArray() {
        return read(byte[].class, super::readByteArray);
    }

    @Override
    public byte[] readByteArray(int len) {
        return read(byte[].class, () -> super.readByteArray(len));
    }

    @Override
    public BlockPos readBlockPos() {
        return read(BlockPos.class, super::readBlockPos);
    }

    @Override
    public BlockHitResult readBlockHitResult() {
        return read(BlockHitResult.class, super::readBlockHitResult);
    }

    @Override
    public BitSet readBitSet() {
        return read(BitSet.class, super::readBitSet);
    }

    @Override
    public boolean readBoolean() {
        return read(Boolean.class, super::readBoolean);
    }

    @Override
    public byte readByte() {
        return read(Byte.class, super::readByte);
    }

    @Override
    public ChunkSectionPos readChunkSectionPos() {
        return read(ChunkSectionPos.class, super::readChunkSectionPos);
    }

    @Override
    public <T extends Enum<T>> T readEnumConstant(Class<T> type) {
        return read(type, () -> super.readEnumConstant(type));
    }

    @Override
    public NbtCompound readNbt() {
        return read(NbtCompound.class, super::readNbt);
    }

    @Override
    public Date readDate() {
        return read(Date.class, super::readDate);
    }

    @Override
    public char readChar() {
        return read(Character.class, super::readChar);
    }

    @Override
    public float readFloat() {
        return read(Float.class, super::readFloat);
    }

    @Override
    public double readDouble() {
        return read(Double.class, super::readDouble);
    }

    @Override
    public ByteBuf readBytes(int len) {
        return read(ByteBuf.class,
                () -> super.readBytes(len),
                () -> super.readByteArray(len),
                value -> {},
                value -> ByteBufAllocator.DEFAULT.buffer(len).writeBytes(value));
    }

    @Override
    public ByteBuf readBytes(ByteBuf out) {
        return read(ByteBuf.class,
                () -> super.readBytes(out),
                () -> super.readByteArray(out.writableBytes()),
                out::writeBytes);
    }

    @Override
    public ByteBuf readBytes(ByteBuf out, int len) {
        return read(ByteBuf.class,
                () -> super.readBytes(out, len),
                () -> super.readByteArray(len),
                out::writeBytes);
    }

    @Override
    public ByteBuf readBytes(ByteBuf out, int index, int len) {
        return read(ByteBuf.class,
                () -> super.readBytes(out, index, len),
                () -> super.readByteArray(len),
                value -> out.writeBytes(value, index, len));
    }

    @Override
    public ByteBuf readBytes(byte[] out) {
        return read(byte[].class,
                () -> super.readBytes(out),
                () -> {
                    super.readBytes(out);
                    return out.clone();
                },
                value -> System.arraycopy(value, 0, out, 0, out.length));
    }

    @Override
    public ByteBuf readBytes(byte[] out, int index, int len) {
        return read(byte[].class,
                () -> super.readBytes(out, index, len),
                () -> {
                    super.readBytes(out, index, len);
                    return Arrays.copyOfRange(out, index, index + len);
                },
                value -> System.arraycopy(value, 0, out, index, len));
    }

    @Override
    public ByteBuf readBytes(ByteBuffer out) {
        return read(ByteBuffer.class,
                () -> super.readBytes(out),
                () -> super.readByteArray(out.remaining()),
                out::put);
    }

    @Override
    public ByteBuf readBytes(OutputStream out, int len) throws IOException {
        try {
            return read(OutputStream.class,
                    () -> {
                        try {
                            return super.readBytes(out, len);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    },
                    () -> super.readByteArray(len),
                    value -> {
                        try {
                            out.write(value);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }

    @Override
    public int readBytes(GatheringByteChannel out, int len) throws IOException {
        try {
            return read(GatheringByteChannel.class, () -> {
                try {
                    return super.readBytes(out, len);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }, () -> super.readByteArray(len), value -> {
                try {
                    ByteBuffer src = ByteBuffer.wrap(value);
                    int n = 0;
                    while (n < len) {
                        n += out.write(src);
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }, value -> len);
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }

    @Override
    public CharSequence readCharSequence(int len, Charset charset) {
        return read(CharSequence.class, () -> super.readCharSequence(len, charset));
    }

    @Override
    public int readBytes(FileChannel out, long pos, int len) throws IOException {
        try {
            return read(FileChannel.class, () -> {
                try {
                    return super.readBytes(out, len);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }, () -> super.readByteArray(len), value -> {
                try {
                    ByteBuffer src = ByteBuffer.wrap(value);
                    int n = 0;
                    while (n < len) {
                        n += out.write(src, pos + n);
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }, value -> len);
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }

    @Override
    public int[] readIntArray() {
        return read(int[].class, super::readIntArray);
    }

    @Override
    public int[] readIntArray(int len) {
        return read(int[].class, () -> super.readIntArray(len));
    }

    @Override
    public long[] readLongArray() {
        return read(long[].class, super::readLongArray);
    }

    @Override
    public long[] readLongArray(long[] out) {
        return read(long[].class,
                () -> super.readLongArray(out),
                () -> super.readLongArray(out).clone(),
                value -> System.arraycopy(value, 0, out, 0, value.length),
                Function.identity());
    }

    @Override
    public long[] readLongArray(long[] out, int len) {
        return read(long[].class,
                () -> super.readLongArray(out, len),
                () -> super.readLongArray(null, len),
                value -> {
                    if (len <= out.length)
                        System.arraycopy(value, 0, out, 0, len);
                },
                value -> len <= out.length ? out : value);
    }

    @Override
    public Text readText() {
        return read(Text.class, super::readText);
    }

    @Override
    public long readVarLong() {
        return read(VarLong.class, () -> new VarLong(super.readVarLong())).get();
    }

    @Override
    public UUID readUuid() {
        return read(UUID.class, super::readUuid);
    }

    @Override
    public ItemStack readItemStack() {
        return read(ItemStack.class, super::readItemStack);
    }

    @Override
    public String readString() {
        return read(String.class, super::readString);
    }

    @Override
    public String readString(int len) {
        return read(String.class, () -> super.readString(len));
    }

    @Override
    public Identifier readIdentifier() {
        return read(Identifier.class, super::readIdentifier);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T decode(Codec<T> codec) {
        // TODO: Make the types here Object rather than class, so we can distinguish the types of Codec
        return read((Class<Codecked<T>>) (Class<?>) Codecked.class, () -> new Codecked<>(codec, super.decode(codec))).getValue();
    }

    // TODO: when non-class types get merged from the 1.8 branch, collections here will need a bit of a refactor.
    // Element type calculation has already been done (albeit not used). The rawtype collection class types will be replaced
    // with a wrapper type in each of these methods, so future transformers can target "collections of V" rather than just
    // "collections". A solution for passthrough reading collections will probably involve creating mirrors of all these
    // collection read methods, which take as a parameter the up-to-date element read method (obtained via accessor mixins),
    // which is similar to the default read methods already provided internally in this class (which call super).

    public <V, C extends Collection<V>> void pendingReadCollection(Class<C> collectionType, Class<V> elementType, C collection) {
        pendingRead(collectionType, collection);
    }

    public <K, V> void pendingReadMap(Class<K> keyClass, Class<V> valueClass, Map<K, V> map) {
        pendingReadMapUnchecked(keyClass, valueClass, map);
    }

    public void pendingReadMapUnchecked(Class<?> keyClass, Class<?> valueClass, Map<?, ?> map) {
        pendingRead(Map.class, map);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T, C extends Collection<T>> C readCollection(IntFunction<C> constructor, Function<PacketByteBuf, T> elementReader) {
        Class<T> elementType = (Class<T>) getLambdaReturnType(elementReader, 1);
        return (C) read(Collection.class, () -> super.readCollection(constructor, elementReader));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> void writeCollection(Collection<T> val, BiConsumer<PacketByteBuf, T> elementWriter) {
        Class<T> elementType = (Class<T>) getLambdaParameterType(elementWriter, 0, 1);
        write((Class<Collection<T>>) (Class<?>) Collection.class, val, c -> super.writeCollection(c, elementWriter));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> List<T> readList(Function<PacketByteBuf, T> elementReader) {
        Class<T> elementType = (Class<T>) getLambdaReturnType(elementReader, 0);
        return read((Class<List<T>>) (Class<?>) List.class, () -> super.readList(elementReader));
    }

    @Override
    public IntList readIntList() {
        return read(IntList.class, super::readIntList);
    }

    @Override
    public void writeIntList(IntList val) {
        write(IntList.class, val, super::writeIntList);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <K, V> Map<K, V> readMap(Function<PacketByteBuf, K> keyReader, Function<PacketByteBuf, V> valueReader) {
        Class<K> keyType = (Class<K>) getLambdaReturnType(keyReader, 0);
        Class<V> valueType = (Class<V>) getLambdaReturnType(valueReader, 1);
        return read((Class<Map<K, V>>) (Class<?>) Map.class, () -> super.readMap(keyReader, valueReader));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <K, V, M extends Map<K, V>> M readMap(IntFunction<M> constructor, Function<PacketByteBuf, K> keyReader, Function<PacketByteBuf, V> valueReader) {
        Class<K> keyType = (Class<K>) getLambdaReturnType(keyReader, 1);
        Class<V> valueType = (Class<V>) getLambdaReturnType(valueReader, 2);
        return read((Class<M>) (Class<?>) Map.class, () -> super.readMap(constructor, keyReader, valueReader));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <K, V> void writeMap(Map<K, V> map, BiConsumer<PacketByteBuf, K> keyWriter, BiConsumer<PacketByteBuf, V> valueWriter) {
        Class<K> keyType = (Class<K>) getLambdaParameterType(keyWriter, 0, 1);
        Class<V> valueType = (Class<V>) getLambdaParameterType(valueWriter, 1, 1);
        write((Class<Map<K, V>>) (Class<?>) Map.class, map, m -> super.writeMap(m, keyWriter, valueWriter));
    }

    private static final Map<Class<?>, Class<?>> lambdaElementTypes = new HashMap<>();
    private static final ReadWriteLock lambdaElementTypeLock = new ReentrantReadWriteLock();

    private Class<?> getLambdaReturnType(Object lambda, int functionalParameterIndex) {
        return getLambdaElementType(lambda, functionalParameterIndex, Type::getReturnType);
    }

    private Class<?> getLambdaParameterType(Object lambda, int functionalParameterIndex, int lambdaParameterIndex) {
        return getLambdaElementType(lambda, functionalParameterIndex, methodType -> methodType.getArgumentTypes()[lambdaParameterIndex]);
    }

    private Class<?> getLambdaElementType(Object lambda, int functionalParameterIndex, UnaryOperator<Type> elementTypeExtractor) {
        if (!transformationEnabled) {
            return null;
        }
        Class<?> lambdaClass = lambda.getClass();

        // quick exit with read lock
        lambdaElementTypeLock.readLock().lock();
        try {
            if (lambdaElementTypes.containsKey(lambdaClass)) {
                return lambdaElementTypes.get(lambdaClass);
            }
        } finally {
            lambdaElementTypeLock.readLock().unlock();
        }

        lambdaElementTypeLock.writeLock().lock();
        try {
            // in case two threads with the same lambda get past the read lock at the same time
            if (lambdaElementTypes.containsKey(lambdaClass)) {
                return lambdaElementTypes.get(lambdaClass);
            }

            StackTraceElement callSite = null;
            for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
                if (!stackTraceElement.getClassName().equals(Thread.class.getName())
                        && !stackTraceElement.getClassName().equals(PacketByteBuf.class.getName())
                        && !stackTraceElement.getClassName().equals(TransformerByteBuf.class.getName())) {
                    callSite = stackTraceElement;
                    break;
                }
            }
            assert callSite != null;

            ClassNode callerClass;
            try {
                callerClass = MixinService.getService().getBytecodeProvider().getClassNode(callSite.getClassName().replace('.', '/'));
            } catch (Exception e) {
                LOGGER.error("Failed to get caller class", e);
                lambdaElementTypes.put(lambdaClass, null);
                return null;
            }

            AbstractInsnNode lineNumberNode = null;
            if (callerClass.methods != null) {
                methodLoop:
                for (MethodNode method : callerClass.methods) {
                    if (method.name.equals(callSite.getMethodName()) && method.instructions != null) {
                        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                            if (insn.getType() == AbstractInsnNode.LINE && ((LineNumberNode) insn).line == callSite.getLineNumber()) {
                                lineNumberNode = insn;
                                break methodLoop;
                            }
                        }
                    }
                }
            }

            if (lineNumberNode == null) {
                LOGGER.error("Failed to find relevant line number " + callSite.getLineNumber() + " in caller class" + callSite.getClassName());
                lambdaElementTypes.put(lambdaClass, null);
                return null;
            }

            int lambdaCount = 0;
            InvokeDynamicInsnNode foundInvokeDynamic = null;
            for (AbstractInsnNode insn = lineNumberNode.getNext(); insn != null; insn = insn.getNext()) {
                if (insn.getOpcode() == Opcodes.INVOKEDYNAMIC) {
                    InvokeDynamicInsnNode invokeDynamic = (InvokeDynamicInsnNode) insn;
                    if (invokeDynamic.bsm.getOwner().equals("java/lang/invoke/LambdaMetafactory") && invokeDynamic.bsm.getName().equals("metafactory")
                            && invokeDynamic.bsmArgs != null && invokeDynamic.bsmArgs.length >= 3
                            && invokeDynamic.bsmArgs[1] instanceof Handle && invokeDynamic.bsmArgs[2] instanceof Type
                            && lambdaCount++ == functionalParameterIndex) {
                        foundInvokeDynamic = invokeDynamic;
                        break;
                    }
                }
            }

            if (foundInvokeDynamic == null) {
                LOGGER.error("Failed to find lambda " + functionalParameterIndex + " in caller class " + callSite.getClassName() + " at line " + callSite.getLineNumber());
                lambdaElementTypes.put(lambdaClass, null);
                return null;
            }

            Handle handle = (Handle) foundInvokeDynamic.bsmArgs[1];
            Type lambdaMethodType = (Type) foundInvokeDynamic.bsmArgs[2];
            if (handle.getOwner().equals(Type.getInternalName(ByteBuf.class))
                    || handle.getOwner().equals(Type.getInternalName(PacketByteBuf.class))
                    || handle.getOwner().equals(Type.getInternalName(TransformerByteBuf.class))) {
                // method reference to a byte buf method, transformer byte buf will handle the type
                lambdaElementTypes.put(lambdaClass, null);
                return null;
            }

            Type elementType = elementTypeExtractor.apply(lambdaMethodType);
            Class<?> lambdaElementType;
            try {
                lambdaElementType = Class.forName(elementType.getSort() == Type.ARRAY ? elementType.getDescriptor() : elementType.getClassName());
            } catch (ClassNotFoundException e) {
                LOGGER.error("Failed to convert lambda element type name " + elementType.getClassName() + " to Class", e);
                lambdaElementTypes.put(lambdaClass, null);
                return null;
            }
            lambdaElementTypes.put(lambdaClass, lambdaElementType);
            return lambdaElementType;
        } finally {
            lambdaElementTypeLock.writeLock().unlock();
        }
    }

    @Override
    public short readUnsignedByte() {
        return read(UnsignedByte.class, () -> new UnsignedByte(super.readUnsignedByte())).get();
    }

    @Override
    public short readShort() {
        return read(Short.class, super::readShort);
    }

    @Override
    public short readShortLE() {
        return read(LEShort.class, () -> new LEShort(super.readShortLE())).get();
    }

    @Override
    public int readUnsignedShort() {
        return read(UnsignedShort.class, () -> new UnsignedShort(super.readUnsignedShort())).get();
    }

    @Override
    public int readUnsignedShortLE() {
        return read(LEUnsignedShort.class, () -> new LEUnsignedShort(super.readUnsignedShortLE())).get();
    }

    @Override
    public int readMedium() {
        return read(Medium.class, () -> new Medium(super.readMedium())).get();
    }

    @Override
    public int readMediumLE() {
        return read(LEMedium.class, () -> new LEMedium(super.readMediumLE())).get();
    }

    @Override
    public int readUnsignedMedium() {
        return read(UnsignedMedium.class, () -> new UnsignedMedium(super.readUnsignedMedium())).get();
    }

    @Override
    public int readUnsignedMediumLE() {
        return read(LEUnsignedMedium.class, () -> new LEUnsignedMedium(super.readUnsignedMediumLE())).get();
    }

    @Override
    public int readInt() {
        return read(Integer.class, super::readInt);
    }

    @Override
    public int readIntLE() {
        return read(LEInteger.class, () -> new LEInteger(super.readIntLE())).get();
    }

    @Override
    public long readUnsignedInt() {
        return read(UnsignedInteger.class, () -> new UnsignedInteger(super.readUnsignedInt())).get();
    }

    @Override
    public long readUnsignedIntLE() {
        return read(LEUnsignedInteger.class, () -> new LEUnsignedInteger(super.readUnsignedIntLE())).get();
    }

    @Override
    public long readLong() {
        return read(Long.class, super::readLong);
    }

    @Override
    public long readLongLE() {
        return read(LELong.class, () -> new LELong(super.readLongLE())).get();
    }

    @Override
    public ByteBuf readSlice(int len) {
        return read(ByteBuf.class, () -> super.readSlice(len));
    }

    @Override
    public ByteBuf readRetainedSlice(int len) {
        return read(ByteBuf.class, () -> super.readRetainedSlice(len));
    }

    @Override
    public PacketByteBuf writeByteArray(byte[] val) {
        return write(byte[].class, val, super::writeByteArray);
    }

    @Override
    public PacketByteBuf writeIntArray(int[] val) {
        return write(int[].class, val, super::writeIntArray);
    }

    @Override
    public PacketByteBuf writeLongArray(long[] val) {
        return write(long[].class, val, super::writeLongArray);
    }

    @Override
    public PacketByteBuf writeBlockPos(BlockPos val) {
        return write(BlockPos.class, val, super::writeBlockPos);
    }

    @Override
    public PacketByteBuf writeEnumConstant(Enum<?> val) {
        return writeEnumConstant0(val);
    }

    @SuppressWarnings("unchecked")
    private <T extends Enum<T>> PacketByteBuf writeEnumConstant0(Enum<T> val) {
        Class<?> clazz = val.getClass();
        for (Class<?> sup = clazz.getSuperclass(); sup != Enum.class; sup = clazz.getSuperclass())
            clazz = sup;
        return write((Class<T>)clazz, (T)val, super::writeEnumConstant);
    }

    @Override
    public PacketByteBuf writeNbt(NbtCompound val) {
        return write(NbtCompound.class, val, super::writeNbt);
    }

    @Override
    public PacketByteBuf writeItemStack(ItemStack val) {
        return write(ItemStack.class, val, super::writeItemStack);
    }

    @Override
    public PacketByteBuf writeIdentifier(Identifier val) {
        return write(Identifier.class, val, super::writeIdentifier);
    }

    @Override
    public PacketByteBuf writeDate(Date val) {
        return write(Date.class, val, super::writeDate);
    }

    @Override
    public void writeBlockHitResult(BlockHitResult val) {
        write(BlockHitResult.class, val, super::writeBlockHitResult);
    }

    @Override
    public void writeBitSet(BitSet val) {
        write(BitSet.class, val, super::writeBitSet);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> void encode(Codec<T> codec, T object) {
        write(Codecked.class, new Codecked<>(codec, object), v -> super.encode(v.getCodec(), v.getValue()));
    }

    @Override
    public ByteBuf writeBoolean(boolean val) {
        return write(Boolean.class, val, super::writeBoolean);
    }

    @Override
    public ByteBuf writeByte(int val) {
        return write(Byte.class, (byte) val, (Consumer<Byte>) super::writeByte);
    }

    @Override
    public ByteBuf writeMedium(int val) {
        return write(Medium.class, new Medium(val), v -> super.writeMedium(v.get()));
    }

    @Override
    public ByteBuf writeMediumLE(int val) {
        return write(LEMedium.class, new LEMedium(val), v -> super.writeMediumLE(v.get()));
    }

    @Override
    public ByteBuf writeInt(int val) {
        return write(Integer.class, val, super::writeInt);
    }

    @Override
    public ByteBuf writeIntLE(int val) {
        return write(LEInteger.class, new LEInteger(val), v -> super.writeIntLE(v.get()));
    }

    @Override
    public ByteBuf writeLong(long val) {
        return write(Long.class, val, super::writeLong);
    }

    @Override
    public ByteBuf writeLongLE(long val) {
        return write(LELong.class, new LELong(val), v -> super.writeLongLE(v.get()));
    }

    @Override
    public ByteBuf writeChar(int val) {
        return write(Character.class, (char) val, (Consumer<Character>) super::writeChar);
    }

    @Override
    public ByteBuf writeFloat(float val) {
        return write(Float.class, val, super::writeFloat);
    }

    @Override
    public ByteBuf writeDouble(double val) {
        return write(Double.class, val, super::writeDouble);
    }

    @Override
    public ByteBuf writeBytes(ByteBuf val) {
        return write(ByteBuf.class, val, super::writeBytes);
    }

    @Override
    public ByteBuf writeBytes(ByteBuf val, int len) {
        return write(ByteBuf.class, val, v -> super.writeBytes(v, len));
    }

    @Override
    public ByteBuf writeBytes(ByteBuf val, int offset, int len) {
        return write(ByteBuf.class, val, v -> super.writeBytes(v, offset, len));
    }

    @Override
    public ByteBuf writeBytes(byte[] val) {
        return write(byte[].class, val, super::writeBytes);
    }

    @Override
    public ByteBuf writeBytes(byte[] val, int offset, int len) {
        return write(byte[].class, val, v -> super.writeBytes(v, offset, len));
    }

    @Override
    public ByteBuf writeBytes(ByteBuffer val) {
        return write(ByteBuffer.class, val, super::writeBytes);
    }

    @Override
    public int writeBytes(InputStream val, int len) throws IOException {
        AtomicInteger written = new AtomicInteger();
        try {
            write(InputStream.class, val, v -> {
                try {
                    written.set(super.writeBytes(v, len));
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
        return written.get();
    }

    @Override
    public int writeBytes(ScatteringByteChannel val, int len) throws IOException {
        AtomicInteger written = new AtomicInteger();
        try {
            write(ScatteringByteChannel.class, val, v -> {
                try {
                    written.set(super.writeBytes(v, len));
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
        return written.get();
    }

    @Override
    public int writeBytes(FileChannel val, long offset, int len) throws IOException {
        AtomicInteger written = new AtomicInteger();
        try {
            write(FileChannel.class, val, v -> {
                try {
                    written.set(super.writeBytes(v, offset, len));
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
        return written.get();
    }

    @Override
    public int writeCharSequence(CharSequence val, Charset charset) {
        AtomicInteger written = new AtomicInteger();
        write(CharSequence.class, val, v -> written.set(super.writeCharSequence(v, charset)));
        return written.get();
    }

    @Override
    public PacketByteBuf writeText(Text val) {
        return write(Text.class, val, super::writeText);
    }

    @Override
    public PacketByteBuf writeUuid(UUID val) {
        return write(UUID.class, val, super::writeUuid);
    }

    @Override
    public PacketByteBuf writeVarLong(long val) {
        return write(VarLong.class, new VarLong(val), v -> super.writeVarLong(v.get()));
    }

    @Override
    public PacketByteBuf writeString(String val) {
        return write(String.class, val, super::writeString);
    }

    @Override
    public PacketByteBuf writeString(String val, int len) {
        return write(String.class, val, v -> super.writeString(v, len));
    }

    @Override
    public ByteBuf writeShort(int val) {
        return write(Short.class, (short) val, (Consumer<Short>) super::writeShort);
    }

    @Override
    public ByteBuf writeShortLE(int val) {
        return write(LEShort.class, new LEShort((short) val), v -> super.writeShortLE(v.get()));
    }

    @Override
    public ByteBuf writeZero(int val) {
        return write(Zero.class, new Zero(val), v -> super.writeZero(v.get()));
    }
}
