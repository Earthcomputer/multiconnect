package net.earthcomputer.multiconnect.protocols.generic;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public final class RegistryBuilder<T> {
    private final SimpleRegistry<T> registry;
    @Nullable
    private final RegistryMutator.RegistryBuilderSupplier builderSupplier;
    private final List<IdList<T>> idLists = new ArrayList<>(1);
    private final Map<T, IdNode<T>> valueToNode = new IdentityHashMap<>();
    private final Map<RegistryKey<T>, IdNode<T>> idToNode = new HashMap<>();
    private final Object2IntMap<T> rawIdCache = new Object2IntOpenCustomHashMap<>(Util.identityHashStrategy());
    private boolean sideEffects;

    public RegistryBuilder(SimpleRegistry<T> registry) {
        this(registry, null);
    }

    public RegistryBuilder(SimpleRegistry<T> registry, @Nullable RegistryMutator.RegistryBuilderSupplier builderSupplier) {
        this.registry = registry;
        this.builderSupplier = builderSupplier;
        for (T value : registry) {
            //noinspection OptionalGetWithoutIsPresent
            register(registry.getRawId(value), value, registry.getKey(value).get(), true);
        }
        this.sideEffects = true;
    }

    private RegistryKey<T> key(Identifier id) {
        return RegistryKey.of(this.registry.getKey(), id);
    }

    // ADDITION METHODS

    public void register(int rawId, T value, String id) {
        register(rawId, value, new Identifier(id));
    }

    public void register(int rawId, T value, Identifier id) {
        register(rawId, value, key(id), false);
    }

    public void registerInPlace(int rawId, T value, String id) {
        registerInPlace(rawId, value, new Identifier(id));
    }

    public void registerInPlace(int rawId, T value, Identifier id) {
        register(rawId, value, key(id), true);
    }

    private void register(int rawId, T value, RegistryKey<T> id, boolean inPlace) {
        // binary search for id list
        int min = 0;
        int max = idLists.size() - 1;
        IdList<T> idList = null;
        int mid = -1;
        while (min <= max) {
            mid = (min + max) / 2;
            IdList<T> list = idLists.get(mid);
            if (rawId >= list.id - 1 && rawId <= list.id + list.size) {
                idList = list;
                break;
            } else if (list.id < rawId) {
                min = mid + 1;
            } else {
                max = mid - 1;
            }
        }

        if (idList == null) {
            IdNode<T> node = new IdNode<>();
            node.value = value;
            node.id = id;
            idList = new IdList<>();
            node.list = idList;
            idList.head = node;
            idList.tail = idList.head;
            idList.id = rawId;
            idList.size = 1;
            this.idLists.add(mid == -1 ? 0 : mid, idList);
            this.valueToNode.put(value, node);
            this.idToNode.put(id, node);
            if (!inPlace) {
                for (int index = mid + 1; index < idLists.size(); index++) {
                    idLists.get(index).id++;
                }
            }
            onAdded(value, inPlace);
            if (!inPlace) {
                rawIdCache.clear();
            }
        } else {
            if (rawId == idList.id - 1 && !inPlace) {
                insertBefore(idList.head, value, id);
            } else if (rawId == idList.id + idList.size) {
                insertAfter(idList.tail, value, id, inPlace);
            } else {
                throw new IllegalArgumentException("Raw ID already taken: " + rawId);
            }
        }
    }

    public void insertBefore(T insertionPoint, T value, String id) {
        insertBefore(insertionPoint, value, new Identifier(id));
    }

    public void insertBefore(T insertionPoint, T value, Identifier id) {
        IdNode<T> insertionNode = valueToNode.get(insertionPoint);
        if (insertionNode == null) {
            throw new IllegalArgumentException("Insertion point not found: " + insertionPoint);
        }
        insertBefore(insertionNode, value, key(id));
    }

    public void insertBefore(RegistryKey<T> insertionPoint, T value, String id) {
        insertBefore(insertionPoint, value, new Identifier(id));
    }

    public void insertBefore(RegistryKey<T> insertionPoint, T value, Identifier id) {
        IdNode<T> insertionNode = idToNode.get(insertionPoint);
        if (insertionNode == null) {
            throw new IllegalArgumentException("Insertion point not found: " + insertionPoint);
        }
        insertBefore(insertionNode, value, key(id));
    }

    private void insertBefore(IdNode<T> insertionNode, T value, RegistryKey<T> id) {
        IdNode<T> node = new IdNode<>();
        node.value = value;
        node.id = id;
        node.list = insertionNode.list;

        node.prev = insertionNode.prev;
        node.next = insertionNode;
        insertionNode.prev = node;
        if (node.prev != null) {
            node.prev.next = node;
        }
        node.list.size++;
        this.valueToNode.put(value, node);
        this.idToNode.put(id, node);

        if (insertionNode == insertionNode.list.head) {
            insertionNode.list.head = node;
        }
        int index = Collections.binarySearch(this.idLists, node.list, Comparator.comparingInt(list -> list.id));
        for (int i = index + 1; i < this.idLists.size(); i++) {
            this.idLists.get(i).id++;
        }
        onAdded(value, false);
        rawIdCache.clear();
    }

    public void insertAfter(T insertionPoint, T value, String id) {
        insertAfter(insertionPoint, value, new Identifier(id));
    }

    public void insertAfter(T insertionPoint, T value, Identifier id) {
        IdNode<T> insertionNode = valueToNode.get(insertionPoint);
        if (insertionNode == null) {
            throw new IllegalArgumentException("Insertion point not found: " + insertionPoint);
        }
        insertAfter(insertionNode, value, key(id), false);
    }

    public void insertAfter(RegistryKey<T> insertionPoint, T value, String id) {
        insertAfter(insertionPoint, value, new Identifier(id));
    }

    public void insertAfter(RegistryKey<T> insertionPoint, T value, Identifier id) {
        IdNode<T> insertionNode = idToNode.get(insertionPoint);
        if (insertionNode == null) {
            throw new IllegalArgumentException("Insertion point not found: " + insertionPoint);
        }
        insertAfter(insertionNode, value, key(id), false);
    }

    public void insertAfterInPlace(T insertionPoint, T value, String id) {
        insertAfterInPlace(insertionPoint, value, new Identifier(id));
    }

    public void insertAfterInPlace(T insertionPoint, T value, Identifier id) {
        IdNode<T> insertionNode = valueToNode.get(insertionPoint);
        if (insertionNode == null) {
            throw new IllegalArgumentException("Insertion point not found: " + insertionPoint);
        }
        insertAfter(insertionNode, value, key(id), true);
    }

    public void insertAfterInPlace(RegistryKey<T> insertionPoint, T value, String id) {
        insertAfterInPlace(insertionPoint, value, new Identifier(id));
    }

    public void insertAfterInPlace(RegistryKey<T> insertionPoint, T value, Identifier id) {
        IdNode<T> insertionNode = idToNode.get(insertionPoint);
        if (insertionNode == null) {
            throw new IllegalArgumentException("Insertion point not found: " + insertionPoint);
        }
        insertAfter(insertionNode, value, key(id), true);
    }

    private void insertAfter(IdNode<T> insertionNode, T value, RegistryKey<T> id, boolean inPlace) {
        IdNode<T> node = new IdNode<>();
        node.value = value;
        node.id = id;
        node.list = insertionNode.list;

        node.prev = insertionNode;
        node.next = insertionNode.next;
        insertionNode.next = node;
        if (node.next != null) {
            node.next.prev = node;
        }
        node.list.size++;
        this.valueToNode.put(value, node);
        this.idToNode.put(id, node);

        if (inPlace) {
            if (insertionNode == insertionNode.list.tail) {
                insertionNode.list.tail = node;
                checkJoinWithNext(insertionNode.list);
            }
        } else {
            int index = Collections.binarySearch(this.idLists, node.list, Comparator.comparingInt(list -> list.id));
            for (int i = index + 1; i < this.idLists.size(); i++) {
                this.idLists.get(i).id++;
            }
        }

        onAdded(value, inPlace);
        if (!inPlace) {
            rawIdCache.clear();
        }
    }

    private void checkJoinWithNext(IdList<T> idList) {
        int index = Collections.binarySearch(this.idLists, idList, Comparator.comparingInt(list -> list.id));
        if (index >= 0) {
            checkJoinLists(index);
        }
    }

    private void checkJoinLists(int index) {
        if (index == this.idLists.size() - 1) {
            return;
        }
        IdList<T> prevList = this.idLists.get(index);
        IdList<T> nextList = this.idLists.get(index + 1);
        if (prevList.id + prevList.size == nextList.id) {
            prevList.tail.next = nextList.head;
            nextList.head.prev = prevList.tail;
            prevList.tail = nextList.tail;
            prevList.size += nextList.size;
            for (IdNode<T> n = nextList.head; n != null; n = n.next) {
                n.list = prevList;
            }
            this.idLists.remove(index + 1);
        }
    }

    @SuppressWarnings("unchecked")
    private void onAdded(T value, boolean inPlace) {
        if (sideEffects && this.builderSupplier != null) {
            ISimpleRegistry<T> registry = (ISimpleRegistry<T>) this.registry;
            for (IRegistryUpdateListener<T> listener : registry.multiconnect_getRegisterListeners()) {
                listener.onUpdate(value, inPlace, this.builderSupplier);
            }
        }
    }

    // CHANGE METHODS

    public void rename(T value, String newId) {
        rename(value, new Identifier(newId));
    }

    public void rename(T value, Identifier newId) {
        IdNode<T> node = valueToNode.get(value);
        if (node == null) {
            throw new IllegalArgumentException("Value not found: " + value);
        }
        rename(node, key(newId));
    }

    public void rename(RegistryKey<T> id, String newId) {
        rename(id, new Identifier(newId));
    }

    public void rename(RegistryKey<T> id, Identifier newId) {
        IdNode<T> node = idToNode.get(id);
        if (node == null) {
            throw new IllegalArgumentException("Id not found: " + id);
        }
        rename(node, key(newId));
    }

    private void rename(IdNode<T> node, RegistryKey<T> newId) {
        if (node.id.equals(newId)) {
            return;
        }
        if (idToNode.containsKey(newId)) {
            throw new IllegalArgumentException("Id already exists: " + newId);
        }
        idToNode.remove(node.id);
        onRemoved(node.value, true);
        node.id = newId;
        idToNode.put(newId, node);
        onAdded(node.value, true);
    }

    // REMOVAL METHODS

    public void unregister(T value) {
        IdNode<T> node = valueToNode.get(value);
        // debug purpose
//        if (value == Items.POWDER_SNOW_BUCKET) {
//            throw new IllegalArgumentException("Powdered Snow Bucket exists: 1" + value);
//
//        }
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        StackTraceElement e = stacktrace[3] ;//maybe this number needs to be corrected
        String methodName = e.getMethodName();

        System.out.println("A value has been unregistered! Value:" + value + ", StackTrace: " + methodName);
        if (node == null) {
            throw new IllegalArgumentException("Value not found: " + value);
        }
        unregister(node);
    }

    public void unregister(RegistryKey<T> id) {
        IdNode<T> node = idToNode.get(id);
        if (node == null) {
            throw new IllegalArgumentException("ID not found: " + id);
        }
        unregister(node);
    }

    private void unregister(IdNode<T> node) {
        if (node.prev != null) {
            node.prev.next = node.next;
        } else {
            node.list.head = node.next;
        }
        if (node.next != null) {
            node.next.prev = node.prev;
        } else {
            node.list.tail = node.prev;
        }
        node.list.size--;
        this.valueToNode.remove(node.value);
        this.idToNode.remove(node.id);

        int index = Collections.binarySearch(this.idLists, node.list, Comparator.comparingInt(list -> list.id));
        for (int i = index + 1; i < this.idLists.size(); i++) {
            this.idLists.get(i).id--;
        }
        if (node.list.size == 0) {
            this.idLists.remove(index);
        }

        onRemoved(node.value, false);
        rawIdCache.clear();
    }

    public void purge(T value) {
        IdNode<T> node = valueToNode.get(value);
        if (node == null) {
            throw new IllegalArgumentException("Value not found: " + value);
        }
        purge(node);
    }

    public void purge(RegistryKey<T> id) {
        IdNode<T> node = idToNode.get(id);
        if (node == null) {
            throw new IllegalArgumentException("ID not found: " + id);
        }
        purge(node);
    }

    private void purge(IdNode<T> node) {
        if (node.prev == null) {
            if (node.next == null) {
                this.idLists.remove(Collections.binarySearch(this.idLists, node.list, Comparator.comparingInt(list -> list.id)));
            } else {
                node.next.prev = null;
                node.list.head = node.next;
                node.list.size--;
                node.list.id++;
            }
        } else {
            if (node.next != null) {
                int index = 0;
                for (IdNode<T> n = node.prev; n != null; n = n.prev) {
                    index++;
                }
                int nextSize = node.list.size - index - 1;
                node.list.size = index;
                IdList<T> nextList = new IdList<>();
                nextList.id = node.list.id + index + 1;
                nextList.size = nextSize;
                nextList.head = node.next;
                nextList.tail = node.list.tail;
                this.idLists.add(1 + Collections.binarySearch(this.idLists, node.list, Comparator.comparingInt(list -> list.id)), nextList);
                node.next.prev = null;
                node.list.tail = node.prev;
            } else {
                node.prev.next = null;
                node.list.tail = node.prev;
                node.list.size--;
            }
        }
        this.valueToNode.remove(node.value);
        this.idToNode.remove(node.id);

        onRemoved(node.value, true);
        rawIdCache.removeInt(node.value);
    }

    public void clear() {
        if (sideEffects) {
            for (int index = this.idLists.size() - 1; index >= 0; index--) {
                for (IdNode<T> node = this.idLists.get(index).tail; node != null; node = node.prev) {
                    onRemoved(node.value, false);
                }
            }
        }

        this.idLists.clear();
        this.valueToNode.clear();
        this.idToNode.clear();
        rawIdCache.clear();
    }

    @SuppressWarnings("unchecked")
    private void onRemoved(T value, boolean inPlace) {
        if (sideEffects && this.builderSupplier != null) {
            ISimpleRegistry<T> registry = (ISimpleRegistry<T>) this.registry;
            for (IRegistryUpdateListener<T> listener : registry.multiconnect_getUnregisterListeners()) {
                listener.onUpdate(value, inPlace, this.builderSupplier);
            }
        }
    }

    // GETTER METHODS

    public int getRawId(T value) {
        IdNode<T> node = valueToNode.get(value);
        if (node == null) {
            throw new IllegalArgumentException("Value not found: " + value);
        }
        return getRawId(node);
    }

    public int getRawId(RegistryKey<T> id) {
        IdNode<T> node = idToNode.get(id);
        if (node == null) {
            throw new IllegalArgumentException("ID not found: " + id);
        }
        return getRawId(node);
    }

    private int getRawId(@NotNull IdNode<T> node) {
        int rawId = rawIdCache.getOrDefault(node.value, -1);
        if (rawId != -1) {
            return rawId;
        }

        IdNode<T> n = node;
        for (; n != null; n = n.prev) {
            rawId = rawIdCache.getOrDefault(n.value, -1);
            if (rawId != -1) {
                break;
            }
        }
        if (n == null) {
            n = node.list.head;
            rawId = node.list.id;
        } else {
            n = n.next;
            rawId++;
        }
        for (; n != node; n = n.next, rawId++) {
            assert n != null;
            rawIdCache.put(n.value, rawId);
        }
        rawIdCache.put(node.value, rawId);
        return rawId;
    }

    public Identifier getId(T value) {
        IdNode<T> node = valueToNode.get(value);
        if (node == null) {
            throw new IllegalArgumentException("Value not found: " + value);
        }
        return node.id.getValue();
    }

    @Nullable
    public T get(Identifier id) {
        return get(key(id));
    }

    @Nullable
    public T get(RegistryKey<T> id) {
        IdNode<T> node = idToNode.get(id);
        return node == null ? null : node.value;
    }

    public boolean contains(Identifier id) {
        return contains(key(id));
    }

    public boolean contains(RegistryKey<T> id) {
        return idToNode.containsKey(id);
    }

    public boolean contains(T value) {
        return valueToNode.containsKey(value);
    }

    public List<T> getEntries() {
        List<T> entries = new ArrayList<>();
        for (IdList<T> list : idLists) {
            for (IdNode<T> node = list.head; node != null; node = node.next) {
                entries.add(node.value);
            }
        }
        return entries;
    }

    public RegistryKey<? extends Registry<T>> getKey() {
        return registry.getKey();
    }

    public <U> RegistryBuilder<U> getOtherBuilder(RegistryKey<? extends Registry<U>> key) {
        if (this.builderSupplier == null) {
            throw new IllegalArgumentException("Cannot call getOtherBuilder on a RegistryBuilder without a builder supplier");
        }
        return this.builderSupplier.get(key);
    }

    // REGISTRY METHODS

    public int getNextId() {
        if (this.idLists.isEmpty()) {
            return 0;
        }
        IdList<T> lastIdList = this.idLists.get(this.idLists.size() - 1);
        return lastIdList.id + lastIdList.size;
    }

    public void enableSideEffects() {
        this.sideEffects = true;
    }

    public void disableSideEffects() {
        this.sideEffects = false;
    }

    public SimpleRegistry<T> createCopiedRegistry() {
        SimpleRegistry<T> registry = new SimpleRegistry<>(this.registry.getKey(), this.registry.getLifecycle(), null);
        for (IdList<T> list : this.idLists) {
            int rawId = list.id;
            for (IdNode<T> node = list.head; node != null; node = node.next) {
                registry.set(rawId++, node.id, node.value, this.registry.getLifecycle());
            }
        }
        registry.freeze();
        return registry;
    }

    @SuppressWarnings("unchecked")
    public void apply() {
        ISimpleRegistry<T> registry = (ISimpleRegistry<T>) this.registry;
        boolean wasFrozen = registry.multiconnect_isFrozen();
        registry.multiconnect_unfreeze();
        registry.multiconnect_clear();

        for (IdList<T> list : this.idLists) {
            int rawId = list.id;
            for (IdNode<T> node = list.head; node != null; node = node.next) {
                this.registry.set(rawId++, node.id, node.value, this.registry.getLifecycle());
            }
        }

        if (wasFrozen) {
            this.registry.freeze();
        }
    }

    private static final class IdList<T> {
        private IdNode<T> head;
        private IdNode<T> tail;
        private int id;
        private int size;
    }

    private static final class IdNode<T> {
        private IdList<T> list;
        @Nullable
        private IdNode<T> prev;
        @Nullable
        private IdNode<T> next;
        private T value;
        private RegistryKey<T> id;
    }
}
