package net.earthcomputer.multiconnect.protocols.generic;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.earthcomputer.multiconnect.api.ThreadSafe;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.impl.DebugUtils;
import net.earthcomputer.multiconnect.impl.TestingAPI;
import net.earthcomputer.multiconnect.impl.Utils;
import net.earthcomputer.multiconnect.protocols.v1_16_5.PendingFullChunkData;
import net.earthcomputer.multiconnect.transformer.TransformerByteBuf;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.OffThreadException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.util.EightWayDirection;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.dimension.DimensionType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChunkDataTranslator {
    private static final Logger LOGGER = LogManager.getLogger("multiconnect");

    private static final AtomicBoolean hasDumpedChunkData = new AtomicBoolean(false);

    private static ExecutorService executor = createExecutor();
    private static final ThreadLocal<ChunkDataTranslator> CURRENT_TRANSLATOR = new ThreadLocal<>();

    @ThreadSafe(withGameThread = false)
    public static ChunkDataTranslator current() {
        return CURRENT_TRANSLATOR.get();
    }

    private final ChunkDataS2CPacket packet;
    private final boolean isFullChunk;
    private final DimensionType dimension;
    private final DynamicRegistryManager registryManager;
    private final List<Packet<ClientPlayPacketListener>> postPackets = new ArrayList<>();
    private final Map<String, Object> userData = new HashMap<>();

    public ChunkDataTranslator(ChunkDataS2CPacket packet, boolean isFullChunk, DimensionType dimension, DynamicRegistryManager registryManager) {
        this.packet = packet;
        this.isFullChunk = isFullChunk;
        this.dimension = dimension;
        this.registryManager = registryManager;
    }

    @ThreadSafe
    public static <T extends Packet<?>> void asyncTranslatePacket(ChannelHandlerContext context, PacketInfo<T> packetInfo, byte[] data) {
        ChunkPos pos = ConnectionInfo.protocol.extractChunkPos(packetInfo.getPacketClass(), new PacketByteBuf(Unpooled.wrappedBuffer(data)));
        executor.submit(new TranslationTask(pos, () -> {
            try {
                TransformerByteBuf buf = new TransformerByteBuf(Unpooled.wrappedBuffer(data), context);
                buf.readTopLevelType(packetInfo.getPacketClass());
                Packet<?> packet = packetInfo.getFactory().apply(buf);
                if (buf.readableBytes() != 0) {
                    throw new IOException("Packet " + packet.getClass().getSimpleName() + " was larger than I expected, found " + buf.readableBytes() + " bytes extra whilst reading packet");
                }
                // handle packet
                ((ChannelInboundHandler) context.handler()).channelRead(context, packet);
            } catch (Throwable e) {
                TestingAPI.onUnexpectedDisconnect(e);
                DebugUtils.logPacketDisconnectError(data);
                LOGGER.error("Failed to async translate packet", e);
                context.disconnect();
            }
        }));
    }

    @ThreadSafe
    public static void asyncExecute(ChunkPos pos, Runnable runnable) {
        executor.submit(new TranslationTask(pos, runnable));
    }

    @ThreadSafe
    public static void submit(ChunkDataS2CPacket packet) {
        MinecraftClient mc = MinecraftClient.getInstance();
        assert mc.world != null;
        ClientPlayNetworkHandler networkHandler = mc.getNetworkHandler();
        assert networkHandler != null;
        boolean isFullChunk = PendingFullChunkData.isFullChunk(new ChunkPos(packet.getX(), packet.getZ()), true);
        DimensionType dimension = mc.world.getDimension();
        ((IChunkDataS2CPacket) packet).multiconnect_setDimension(dimension);
        ChunkDataTranslator translator = new ChunkDataTranslator(packet, isFullChunk, dimension, networkHandler.getRegistryManager());
        executor.submit(new TranslationTask(new ChunkPos(packet.getX(), packet.getZ()), () -> {
            try {
                CURRENT_TRANSLATOR.set(translator);

                TransformerByteBuf buf = new TransformerByteBuf(packet.getReadBuffer(), null);
                buf.readTopLevelType(ChunkData.class);
                ChunkData chunkData = ChunkData.read(dimension.getMinimumY(),
                    dimension.getMinimumY() + dimension.getHeight() - 1, buf);

                var blocksNeedingConnectionUpdate = new EnumMap<EightWayDirection, IntSet>(EightWayDirection.class);
                ConnectionInfo.protocol.getBlockConnector().fixChunkData(chunkData, blocksNeedingConnectionUpdate);
                ((IChunkDataS2CPacket) packet).multiconnect_setBlocksNeedingUpdate(blocksNeedingConnectionUpdate);

                ConnectionInfo.protocol.postTranslateChunk(translator, chunkData);
                ((IChunkDataS2CPacket) packet).setData(chunkData.toByteArray());

                CURRENT_TRANSLATOR.set(null);

                ((IChunkDataS2CPacket) packet).multiconnect_setDataTranslated(true);
                try {
                    networkHandler.onChunkData(packet);
                } catch (OffThreadException ignore) {
                }

                for (var postPacket : translator.postPackets) {
                    try {
                        postPacket.apply(networkHandler);
                    } catch (OffThreadException ignore) {
                    }
                }
            } catch (Throwable e) {
                if (!hasDumpedChunkData.getAndSet(true)) {
                    DebugUtils.logPacketDisconnectError(packet.getReadBuffer().array());
                }
                LOGGER.error("Failed to translate chunk " + packet.getX() + ", " + packet.getZ(), e);
            }
        }));
    }

    public static void clear() {
        executor.shutdownNow();
        try {
            //noinspection ResultOfMethodCallIgnored
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            // meh
        }
        executor = createExecutor();
    }

    public ChunkDataS2CPacket getPacket() {
        return packet;
    }

    public boolean isFullChunk() {
        return isFullChunk;
    }

    public DimensionType getDimension() {
        return dimension;
    }

    public DynamicRegistryManager getRegistryManager() {
        return registryManager;
    }

    public List<Packet<ClientPlayPacketListener>> getPostPackets() {
        return postPackets;
    }

    public Object getUserData(String key) {
        return userData.get(key);
    }

    public void setUserData(String key, Object value) {
        userData.put(key, value);
    }

    @SuppressWarnings("unchecked") // some evil stuff
    private static ExecutorService createExecutor() {
        int numThreads = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
        BlockingQueue<Runnable> queue = (BlockingQueue<Runnable>) (BlockingQueue<?>) new TranslationQueue();
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("multiconnect chunk translator #%d").build();
        return new ThreadPoolExecutor(numThreads, numThreads, 0, TimeUnit.MILLISECONDS, queue, threadFactory) {
            @Override
            protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
                RunnableFuture<T> delegate = super.newTaskFor(runnable, value);
                return new TranslationFutureTask<>(delegate, ((IHasChunkPos) runnable).pos());
            }

            @Override
            protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
                throw new UnsupportedOperationException();
            }
        };
    }

    private interface IHasChunkPos {
        ChunkPos pos();
    }

    private record TranslationTask(ChunkPos pos, Runnable task) implements Runnable, IHasChunkPos {
        @Override
        public void run() {
            task.run();
        }
    }

    private record TranslationFutureTask<V>(RunnableFuture<V> delegate, ChunkPos pos) implements RunnableFuture<V>, IHasChunkPos {
        @Override
        public void run() {
            delegate.run();
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return delegate.cancel(mayInterruptIfRunning);
        }

        @Override
        public boolean isCancelled() {
            return delegate.isCancelled();
        }

        @Override
        public boolean isDone() {
            return delegate.isDone();
        }

        @Override
        public V get() throws InterruptedException, ExecutionException {
            return delegate.get();
        }

        @Override
        public V get(long timeout, @NotNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return delegate.get(timeout, unit);
        }
    }

    private static class TranslationQueue implements BlockingQueue<IHasChunkPos>, Comparator<IHasChunkPos> {
        private final List<IHasChunkPos> queue = new ArrayList<>();
        private final Semaphore semaphore = new Semaphore(0);
        private ChunkPos lastPlayerPos = new ChunkPos(0, 0);

        @Override
        public int compare(IHasChunkPos o1, IHasChunkPos o2) {
            int distance1 = Math.abs(o1.pos().x - lastPlayerPos.x) + Math.abs(o1.pos().z - lastPlayerPos.z);
            int distance2 = Math.abs(o2.pos().x - lastPlayerPos.x) + Math.abs(o2.pos().z - lastPlayerPos.z);
            return Integer.compare(distance1, distance2);
        }

        @Override
        public boolean add(@NotNull IHasChunkPos translationTask) {
            return offer(translationTask);
        }

        @Override
        public synchronized boolean offer(@NotNull IHasChunkPos translationTask) {
            ensureSorted();
            Utils.heapAdd(queue, translationTask, this);
            semaphore.release();
            return true;
        }

        @Override
        public void put(@NotNull IHasChunkPos translationTask) throws InterruptedException {
            offer(translationTask);
        }

        @Override
        public boolean offer(IHasChunkPos translationTask, long timeout, @NotNull TimeUnit unit) {
            return offer(translationTask);
        }

        @NotNull
        @Override
        public IHasChunkPos take() throws InterruptedException {
            semaphore.acquire();
            //noinspection ConstantConditions
            return poll0();
        }

        @Nullable
        @Override
        public IHasChunkPos poll(long timeout, @NotNull TimeUnit unit) throws InterruptedException {
            if (!semaphore.tryAcquire(timeout, unit)) {
                return null;
            }
            return poll0();
        }

        @Override
        public int remainingCapacity() {
            return Integer.MAX_VALUE;
        }

        @Override
        public synchronized boolean remove(Object o) {
            ensureSorted();
            if (semaphore.tryAcquire() && queue.remove(o)) {
                Utils.heapify(queue, this);
                return true;
            } else {
                return false;
            }
        }

        @Override
        public synchronized boolean contains(Object o) {
            return queue.contains(o);
        }

        @Override
        public int drainTo(@NotNull Collection<? super IHasChunkPos> c) {
            return 0; // unsupported
        }

        @Override
        public int drainTo(@NotNull Collection<? super IHasChunkPos> c, int maxElements) {
            return 0; // unsupported
        }

        @Override
        public IHasChunkPos remove() {
            IHasChunkPos result = poll();
            if (result == null) {
                throw new NoSuchElementException();
            }
            return result;
        }

        @Override
        public IHasChunkPos poll() {
            if (semaphore.tryAcquire()) {
                return poll0();
            } else {
                return null;
            }
        }

        private synchronized IHasChunkPos poll0() {
            ensureSorted();
            if (queue.isEmpty()) {
                return null;
            }
            return Utils.heapRemove(queue, this);
        }

        @Override
        public IHasChunkPos element() {
            IHasChunkPos result = peek();
            if (result == null) {
                throw new NoSuchElementException();
            }
            return result;
        }

        @Override
        public synchronized IHasChunkPos peek() {
            ensureSorted();
            if (queue.isEmpty()) {
                return null;
            }
            return queue.get(0);
        }

        @Override
        public int size() {
            return queue.size();
        }

        @Override
        public boolean isEmpty() {
            return queue.isEmpty();
        }

        @NotNull
        @Override
        public Iterator<IHasChunkPos> iterator() {
            return queue.iterator();
        }

        @NotNull
        @Override
        public synchronized Object @NotNull [] toArray() {
            return queue.toArray();
        }

        @NotNull
        @Override
        public synchronized <T> T @NotNull [] toArray(@NotNull T @NotNull [] a) {
            return queue.toArray(a);
        }

        @Override
        public synchronized boolean containsAll(@NotNull Collection<?> c) {
            return queue.containsAll(c);
        }

        @Override
        public synchronized boolean addAll(@NotNull Collection<? extends IHasChunkPos> c) {
            ensureSorted();
            if (queue.addAll(c)) {
                Utils.heapify(queue, this);
                return true;
            } else {
                return false;
            }
        }

        @Override
        public synchronized boolean removeAll(@NotNull Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public synchronized boolean retainAll(@NotNull Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public synchronized void clear() {
            semaphore.drainPermits();
            queue.clear();
        }

        private void ensureSorted() {
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (player != null) {
                ChunkPos playerPos = player.getChunkPos();
                if (!playerPos.equals(lastPlayerPos)) {
                    lastPlayerPos = playerPos;
                    Utils.heapify(queue, this);
                }
            }
        }
    }
}
