package net.earthcomputer.multiconnect.impl;

import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MulticonnectScheduler {
    private static long tickCounter = 0;

    private static final Int2ObjectMap<List<Task>> tasksByPeriod = new Int2ObjectAVLTreeMap<>();

    public static void tick() {
        var mapItr = tasksByPeriod.int2ObjectEntrySet().iterator();
        while (mapItr.hasNext()) {
            var entry = mapItr.next();
            if (tickCounter % entry.getIntKey() == 0) {
                var itr = entry.getValue().iterator();
                boolean empty = true;
                while (itr.hasNext()) {
                    Task task = itr.next();
                    if (task.isComplete()) {
                        itr.remove();
                    } else {
                        task.run();
                        empty = false;
                    }
                }
                if (empty) {
                    mapItr.remove();
                }

            }
        }
        tickCounter++;
    }

    public static void schedule(Runnable task) {
        schedule(1, task);
    }

    public static void schedule(int period, Runnable task) {
        tasksByPeriod.computeIfAbsent(period, k -> new ArrayList<>()).add(new Task() {
            @Override
            public void run() {
                task.run();
            }

            @Override
            public boolean isComplete() {
                return false;
            }
        });
    }

    public static <T> void scheduleWeak(@NotNull T object, Consumer<? super T> task) {
        scheduleWeak(1, object, task);
    }

    public static <T> void scheduleWeak(int period, @NotNull T object, Consumer<? super T> task) {
        WeakReference<T> weakObject = new WeakReference<>(object);
        tasksByPeriod.computeIfAbsent(period, k -> new ArrayList<>()).add(new Task() {
            @Override
            public void run() {
                T object = weakObject.get();
                if (object != null) {
                    task.accept(object);
                }
            }

            @Override
            public boolean isComplete() {
                return weakObject.get() == null;
            }
        });
    }

    private interface Task extends Runnable {
        boolean isComplete();
    }
}
