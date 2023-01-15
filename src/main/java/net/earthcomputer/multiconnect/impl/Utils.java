package net.earthcomputer.multiconnect.impl;

import com.google.common.cache.Cache;
import net.earthcomputer.multiconnect.api.ThreadSafe;
import net.earthcomputer.multiconnect.api.IProtocol;
import net.earthcomputer.multiconnect.connect.ConnectionMode;
import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import java.lang.ref.Cleaner;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Utils {

    public static DropDownWidget<IProtocol> createVersionDropdown(Screen screen, IProtocol initialMode) {
        var versionDropDown = new DropDownWidget<>(screen.width - 80, 5, 75, 20, initialMode, mode -> {
            MutableComponent text = Component.literal(mode.getName());
            if (mode.isMulticonnectBeta()) {
                text.append(Component.literal(" !").withStyle(ChatFormatting.RED));
            }
            if (mode.isMulticonnectExtension()) {
                text.append(Component.literal(" e").withStyle(ChatFormatting.GOLD));
            }
            return text;
        })
                .setCategoryLabelExtractor(mode -> {
                    MutableComponent text = Component.literal(mode.getMajorReleaseName());
                    if (mode.isMulticonnectBeta()) {
                        text.append(Component.literal(" !").withStyle(ChatFormatting.RED));
                    }
                    if (mode.isMulticonnectExtension()) {
                        text.append(Component.literal(" e").withStyle(ChatFormatting.GOLD));
                    }
                    return text;
                })
                .setTooltipRenderer((matrices, mode, x, y, isCategory) -> {
                    final List<Component> tooltip = new ArrayList<>();
                    if (mode.isMulticonnectBeta()) {
                        String modeName = isCategory ? mode.getMajorReleaseName() : mode.getName();
                        tooltip.add(Component.translatable("multiconnect.betaWarning.line1", modeName));
                        if (!mode.isMulticonnectExtension()) {
                            tooltip.add(Component.translatable("multiconnect.betaWarning.line2", modeName));
                        }
                    }
                    if (mode.isMulticonnectExtension()) {
                        String modeName = isCategory ? mode.getMajorReleaseName() : mode.getName();
                        tooltip.add(Component.translatable("multiconnect.extensionWarning.line1", modeName));
                        tooltip.add(Component.translatable("multiconnect.extensionWarning.line2", modeName));
                    }
                    if (!tooltip.isEmpty()) {
                        screen.renderComponentTooltip(matrices, tooltip, x, y);
                    }
                });

        // populate the versions
        populateDropdown(versionDropDown, ConnectionMode.AUTO);
        for (IProtocol protocol : ProtocolRegistry.getProtocols()) {
            populateDropdown(versionDropDown, protocol);
        }

        return versionDropDown;
    }

    private static void populateDropdown(DropDownWidget<IProtocol> versionDropDown, IProtocol protocol) {
        if (protocol.isMajorRelease()) {
            var category = versionDropDown.add(protocol);
            List<IProtocol> children = protocol.getMinorReleases();
            if (children.size() > 1) {
                for (IProtocol child : children) {
                    category.add(child);
                }
            }
        }
    }

    @ThreadSafe
    public static void leftShift(BitSet bitSet, int n) {
        if (n < 0) {
            rightShift(bitSet, -n);
        } else if (n > 0) {
            for (int i = bitSet.length(); (i = bitSet.previousSetBit(i - 1)) != -1;) {
                bitSet.set(i + n);
                bitSet.clear(i);
            }
        }
    }

    @ThreadSafe
    public static void rightShift(BitSet bitSet, int n) {
        if (n < 0) {
            leftShift(bitSet, -n);
        } else if (n > 0) {
            for (int i = bitSet.nextSetBit(n); i != -1; i = bitSet.nextSetBit(i + 1)) {
                bitSet.set(i - n);
                bitSet.clear(i);
            }
        }
    }

    private static final ScheduledExecutorService AUTO_CACHE_CLEAN_EXECUTOR = Executors.newScheduledThreadPool(1);
    private static final Cleaner AUTO_CACHE_CLEANER = Cleaner.create();
    public static void autoCleanUp(Cache<?, ?> cache, long time, TimeUnit timeUnit) {
        WeakReference<Cache<?, ?>> weakCache = new WeakReference<>(cache);
        ScheduledFuture<?> autoCleanTask = AUTO_CACHE_CLEAN_EXECUTOR.scheduleAtFixedRate(() -> {
            Cache<?, ?> c = weakCache.get();
            if (c != null) {
                c.cleanUp();
            }
        }, time, time, timeUnit);
        AUTO_CACHE_CLEANER.register(cache, () -> autoCleanTask.cancel(false));
    }

    @ThreadSafe
    public static String toString(Object o) {
        if (o == null || !o.getClass().isArray()) {
            return String.valueOf(o);
        }

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0, e = Array.getLength(o); i < e; i++) {
            if (i != 0) {
                sb.append(", ");
            }
            sb.append(toString(Array.get(o, i)));
        }

        return sb.append("]").toString();
    }

    @ThreadSafe
    public static String toString(Object o, int maxLen) {
        String str = toString(o);
        if (str.length() > maxLen && maxLen > "...".length()) {
            return str.substring(0, maxLen - "...".length()) + "...";
        }
        return str;
    }

    @ThreadSafe
    public static <T extends Comparable<T>> void heapify(List<T> list) {
        heapify(list, Comparator.naturalOrder());
    }

    @ThreadSafe
    public static <T> void heapify(List<T> list, Comparator<? super T> comparator) {
        // See: PriorityQueue.heapify
        int n = list.size();
        for (int i = (n >>> 1) - 1; i >= 0; i--) {
            heapSiftDown(list, i, comparator);
        }
    }

    @ThreadSafe
    public static <T extends Comparable<T>> void heapAdd(List<T> list, T element) {
        heapAdd(list, element, Comparator.naturalOrder());
    }

    @ThreadSafe
    public static <T> void heapAdd(List<T> list, T element, Comparator<? super T> comparator) {
        list.add(element);
        heapSiftUp(list, list.size() - 1, comparator);
    }

    @ThreadSafe
    public static <T extends Comparable<T>> T heapRemove(List<T> list) {
        return heapRemove(list, Comparator.naturalOrder());
    }

    @ThreadSafe
    public static <T> T heapRemove(List<T> list, Comparator<? super T> comparator) {
        if (list.size() <= 1) {
            return list.remove(0);
        }
        T result = list.set(0, list.remove(list.size() - 1));
        heapSiftDown(list, 0, comparator);
        return result;
    }

    @ThreadSafe
    private static <T> void heapSiftDown(List<T> list, int k, Comparator<? super T> comparator) {
        // See: PriorityQueue.siftDown
        int n = list.size();
        int half = n >>> 1;
        T x = list.get(k);
        while (k < half) {
            int child = (k << 1) + 1;
            T c = list.get(child);
            int right = child + 1;
            if (right < n && comparator.compare(c, list.get(right)) > 0) {
                child = right;
                c = list.get(child);
            }
            if (comparator.compare(x, c) <= 0) {
                break;
            }
            list.set(k, c);
            k = child;
        }
        list.set(k, x);
    }

    @ThreadSafe
    private static <T> void heapSiftUp(List<T> list, int k, Comparator<? super T> comparator) {
        // See: PriorityQueue.siftUp
        T x = list.get(k);
        while (k > 0) {
            int parent = (k - 1) >>> 1;
            T e = list.get(parent);
            if (comparator.compare(x, e) >= 0) {
                break;
            }
            list.set(k, e);
            k = parent;
        }
        list.set(k, x);
    }
}
