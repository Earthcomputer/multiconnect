package net.earthcomputer.multiconnect.impl;

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.util.Util;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public final class ReadWritePacketExecutor {
    private final ReadWriteBlockingQueue queue;
    private final ExecutorService executor;
    private final ArrayDeque<MutableObject<Runnable>> slidingWindow = new ArrayDeque<>();
    private boolean isShutdown;

    @SuppressWarnings("unchecked") // some evil stuff
    public ReadWritePacketExecutor(boolean clientbound) {
        String direction = clientbound ? "clientbound" : "serverbound";
        int numThreads = Math.max(1, (Runtime.getRuntime().availableProcessors() - 1) / 2);
        this.queue = new ReadWriteBlockingQueue();
        var queue = (BlockingQueue<Runnable>) (BlockingQueue<?>) this.queue;
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("multiconnect " + direction + " translator #%d").build();
        executor = new ThreadPoolExecutor(numThreads, numThreads, 0, TimeUnit.MILLISECONDS, queue, threadFactory) {
            @Override
            protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
                RunnableFuture<T> delegate = super.newTaskFor(runnable, value);
                IHasPacket hasPacket = (IHasPacket) runnable;
                return new TranslationFutureTask<>(delegate, hasPacket.readDependencies(), hasPacket.writeDependencies());
            }

            @Override
            protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
                throw new UnsupportedOperationException();
            }
        };
    }

    public void submit(
            Class<?>[] readDependencies,
            Class<?>[] writeDependencies,
            Runnable translation,
            Runnable onTranslated
    ) {
        if (isShutdown) {
            return;
        }

        MutableObject<Runnable> slidingWindowEntry = new MutableObject<>();
        synchronized (slidingWindow) {
            slidingWindow.offer(slidingWindowEntry);
        }
        executor.submit(new TranslationTask(readDependencies, writeDependencies, () -> {
            try {
                translation.run();

                if (isShutdown) {
                    return;
                }

                synchronized (slidingWindow) {
                    slidingWindowEntry.setValue(onTranslated);
                    MutableObject<Runnable> entry;
                    while ((entry = slidingWindow.peek()) != null && entry.getValue() != null) {
                        //noinspection ConstantConditions
                        slidingWindow.poll().getValue().run();
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                for (Class<?> read : readDependencies) {
                    queue.finishedReadAccess(read);
                }
                for (Class<?> write : writeDependencies) {
                    queue.finishedWriteAccess(write);
                }
            }
        }));
    }

    public void shutdown() {
        isShutdown = true;
        executor.shutdownNow();
    }

    public CompletableFuture<Void> awaitTermination(long start) {
        return CompletableFuture.completedFuture(null).thenRun(() -> {
            long now = System.nanoTime();
            try {
                //noinspection ResultOfMethodCallIgnored
                executor.awaitTermination(5_000_000_000L - (now - start), TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                // meh
            }
        });
    }
}

final class ReadWriteBlockingQueue implements BlockingQueue<IHasPacket> {
    private final List<IHasPacket> packets = new ArrayList<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition canTake = lock.newCondition();
    private final Set<Class<?>> writeAccesses = Sets.newIdentityHashSet();
    private final Object2IntMap<Class<?>> readAccesses = new Object2IntOpenCustomHashMap<>(Util.identityHashStrategy());

    public void finishedWriteAccess(Class<?> clazz) {
        lock.lock();
        try {
            if (writeAccesses.remove(clazz)) {
                canTake.signalAll(); // may potentially unblock multiple tasks, need to signal all
            }
        } finally {
            lock.unlock();
        }
    }

    public void finishedReadAccess(Class<?> clazz) {
        lock.lock();
        try {
            int result = readAccesses.mergeInt(clazz, -1, Integer::sum);
            if (result < 0) {
                throw new IllegalStateException("Unlocked a read access without locking it");
            }
            if (result == 0) {
                readAccesses.removeInt(clazz);
                canTake.signal();
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean add(@NotNull IHasPacket packet) {
        return offer(packet);
    }

    @Override
    public boolean offer(@NotNull IHasPacket packet) {
        lock.lock();
        try {
            packets.add(packet);
            canTake.signal();
        } finally {
            lock.unlock();
        }
        return true;
    }

    @Override
    public IHasPacket remove() {
        IHasPacket packet = poll();
        if (packet == null) {
            throw new NoSuchElementException();
        }
        return packet;
    }

    @Override
    public IHasPacket poll() {
        lock.lock();
        try {
            IHasPacket element = peek();
            if (element == null) {
                return null;
            }
            packets.remove(element);
            Collections.addAll(writeAccesses, element.writeDependencies());
            for (Class<?> read : element.readDependencies()) {
                readAccesses.mergeInt(read, 1, Integer::sum);
            }
            return element;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public IHasPacket element() {
        IHasPacket element = peek();
        if (element == null) {
            throw new NoSuchElementException();
        }
        return element;
    }

    @Override
    public IHasPacket peek() {
        ObjectSet<Class<?>> readAccesses = new ObjectOpenCustomHashSet<>(this.readAccesses.keySet(), Util.identityHashStrategy());
        ObjectSet<Class<?>> writeAccesses = new ObjectOpenCustomHashSet<>(this.writeAccesses, Util.identityHashStrategy());
        lock.lock();
        try {
            for (IHasPacket packet : packets) {
                continuePacketLoop: {
                    for (Class<?> write : packet.writeDependencies()) {
                        if (writeAccesses.contains(write) || readAccesses.contains(write)) {
                            break continuePacketLoop;
                        }
                    }
                    for (Class<?> read : packet.readDependencies()) {
                        if (writeAccesses.contains(read)) {
                            break continuePacketLoop;
                        }
                    }
                    return packet;
                }
                Collections.addAll(readAccesses, packet.readDependencies());
                Collections.addAll(writeAccesses, packet.writeDependencies());
            }
            return null;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void put(@NotNull IHasPacket packet) {
        offer(packet);
    }

    @Override
    public boolean offer(IHasPacket packet, long timeout, @NotNull TimeUnit unit) {
        return offer(packet);
    }

    @NotNull
    @Override
    public IHasPacket take() throws InterruptedException {
        lock.lockInterruptibly();
        try {
            IHasPacket packet;
            while ((packet = poll()) == null) {
                canTake.await();
            }
            return packet;
        } finally {
            lock.unlock();
        }
    }

    @Nullable
    @Override
    public IHasPacket poll(long timeout, @NotNull TimeUnit unit) throws InterruptedException {
        lock.lockInterruptibly();
        try {
            IHasPacket packet;
            long nanos = unit.toNanos(timeout);
            while ((packet = poll()) == null) {
                if (nanos <= 0) {
                    return null;
                }
                nanos = canTake.awaitNanos(nanos);
            }
            return packet;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int remainingCapacity() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        //noinspection SlowListContainsAll
        return packets.containsAll(c);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends IHasPacket> c) {
        for (IHasPacket packet : c) {
            add(packet);
        }
        return !c.isEmpty();
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        while (poll() != null);
    }

    @Override
    public int size() {
        return packets.size();
    }

    @Override
    public boolean isEmpty() {
        return packets.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return packets.contains(o);
    }

    @NotNull
    @Override
    public Iterator<IHasPacket> iterator() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Object @NotNull [] toArray() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public <T> T @NotNull [] toArray(@NotNull T @NotNull [] a) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int drainTo(@NotNull Collection<? super IHasPacket> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int drainTo(@NotNull Collection<? super IHasPacket> c, int maxElements) {
        throw new UnsupportedOperationException();
    }
}

interface IHasPacket {
    Class<?>[] readDependencies();
    Class<?>[] writeDependencies();
}

record TranslationTask(Class<?>[] readDependencies, Class<?>[] writeDependencies, Runnable task) implements Runnable, IHasPacket {
    @Override
    public void run() {
        task.run();
    }
}

record TranslationFutureTask<V>(RunnableFuture<V> delegate, Class<?>[] readDependencies, Class<?>[] writeDependencies) implements RunnableFuture<V>, IHasPacket {
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
