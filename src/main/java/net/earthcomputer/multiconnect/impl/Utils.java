package net.earthcomputer.multiconnect.impl;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.earthcomputer.multiconnect.connect.ConnectionMode;
import net.earthcomputer.multiconnect.mixin.bridge.TrackedDataHandlerRegistryAccessor;
import net.earthcomputer.multiconnect.protocols.generic.*;
import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.datafixer.NbtOps;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.collection.Int2ObjectBiMap;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Utils {
    public static CompoundTag datafix(DSL.TypeReference type, CompoundTag old) {
        return (CompoundTag) datafix(type, NbtOps.INSTANCE, old);
    }

    public static <T> T datafix(DSL.TypeReference type, DynamicOps<T> ops, T old) {
        int oldVersion = ConnectionMode.byValue(ConnectionInfo.protocolVersion).getDataVersion();
        int currentVersion = SharedConstants.getGameVersion().getWorldVersion();
        if (oldVersion == currentVersion) {
            return old;
        }
        DataFixer fixer = MinecraftClient.getInstance().getDataFixer();
        Dynamic<T> translated = fixer.update(type, new Dynamic<>(ops, old), oldVersion, currentVersion);
        return translated.getValue();
    }

    @SuppressWarnings("unchecked")
    public static List<TypeRewriteRule> getRules(TypeRewriteRule.Seq seq) {
        try {
            Field field = TypeRewriteRule.Seq.class.getDeclaredField("rules");
            field.setAccessible(true);
            return (List<TypeRewriteRule>) field.get(seq);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    @SafeVarargs
    public static <T, U> Comparator<T> orderBy(Function<T, U> mapper, U... order) {
        ImmutableMap.Builder<U, Integer> indexBuilder = ImmutableMap.builder();
        for (int i = 0; i < order.length; i++) {
            indexBuilder.put(order[i], i);
        }
        ImmutableMap<U, Integer> indexes = indexBuilder.build();
        Integer absent = indexes.size();
        return Comparator.comparing(val -> indexes.getOrDefault(mapper.apply(val), absent));
    }

    public static void insertAfter(List<PacketInfo<?>> list, Class<? extends Packet<?>> element, PacketInfo<?> toInsert) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getPacketClass() == element) {
                list.add(i + 1, toInsert);
                return;
            }
        }
        list.add(0, toInsert);
    }

    public static <T> void insertAfter(List<T> list, T element, T toInsert) {
        list.add(list.indexOf(element) + 1, toInsert);
    }

    public static <T> void insertAfter(ISimpleRegistry<T> registry, T element, T toInsert, String id) {
        insertAfter(registry, element, toInsert, id, false);
    }

    @SuppressWarnings("unchecked")
    public static <T> void insertAfter(ISimpleRegistry<T> registry, T element, T toInsert, String id, boolean inPlace) {
        RegistryKey<T> key = RegistryKey.of(registry.getRegistryKey(), new Identifier(id));
        int numericalId = ((SimpleRegistry<T>) registry).getRawId(element) + 1;
        if (inPlace) {
            registry.registerInPlace(toInsert, numericalId, key);
        } else {
            registry.register(toInsert, numericalId, key);
        }
    }

    public static void remove(List<PacketInfo<?>> list, Class<? extends Packet<?>> element) {
        list.removeIf(it -> it.getPacketClass() == element);
    }

    public static void removeTrackedDataHandler(TrackedDataHandler<?> handler) {
        Int2ObjectBiMap<TrackedDataHandler<?>> biMap = TrackedDataHandlerRegistryAccessor.getHandlers();
        //noinspection unchecked
        IInt2ObjectBiMap<TrackedDataHandler<?>> iBiMap = (IInt2ObjectBiMap<TrackedDataHandler<?>>) biMap;
        int id = TrackedDataHandlerRegistry.getId(handler);
        iBiMap.multiconnect_remove(handler);
        for (; TrackedDataHandlerRegistry.get(id + 1) != null; id++) {
            TrackedDataHandler<?> h = TrackedDataHandlerRegistry.get(id + 1);
            iBiMap.multiconnect_remove(h);
            biMap.put(h, id);
        }
    }

    public static void copyBlocks(TagRegistry<Item> tags, TagRegistry<Block> blockTags, Tag.Identified<Item> tag, Tag.Identified<Block> blockTag) {
        tags.add(tag, Collections2.transform(blockTags.get(blockTag.getId()), Block::asItem));
    }

    @SuppressWarnings("unchecked")
    public static <T> int getUnmodifiedId(Registry<T> registry, T value) {
        DefaultRegistries<T> defaultRegistries = (DefaultRegistries<T>) DefaultRegistries.DEFAULT_REGISTRIES.get(registry);
        if (defaultRegistries == null) return registry.getRawId(value);
        return defaultRegistries.defaultIndexedEntries.getId(value);
    }

    @SuppressWarnings("unchecked")
    public static <T> Identifier getUnmodifiedName(Registry<T> registry, T value) {
        DefaultRegistries<T> defaultRegistries = (DefaultRegistries<T>) DefaultRegistries.DEFAULT_REGISTRIES.get(registry);
        if (defaultRegistries == null) return registry.getId(value);
        return defaultRegistries.defaultEntriesById.inverse().get(value);
    }

    @SuppressWarnings("unchecked")
    public static <T> void rename(ISimpleRegistry<T> registry, T value, String newName) {
        int id = ((SimpleRegistry<T>) registry).getRawId(value);
        registry.purge(value);
        RegistryKey<T> key = RegistryKey.of(registry.getRegistryKey(), new Identifier(newName));
        registry.registerInPlace(value, id, key);
    }

    @SuppressWarnings("unchecked")
    public static <T> void reregister(ISimpleRegistry<T> registry, T value, boolean inPlace) {
        if (registry.getEntriesById().containsValue(value))
            return;

        //noinspection SuspiciousMethodCalls
        DefaultRegistries<T> defaultRegistries = (DefaultRegistries<T>) DefaultRegistries.DEFAULT_REGISTRIES.get(registry);
        T prevValue = null;
        for (int id = defaultRegistries.defaultIndexedEntries.getId(value) - 1; id >= 0; id--) {
            T val = defaultRegistries.defaultIndexedEntries.get(id);
            if (registry.getEntriesById().containsValue(val)) {
                prevValue = val;
                break;
            }
        }

        insertAfter(registry, prevValue, value, defaultRegistries.defaultEntriesById.inverse().get(value).toString(), inPlace);
    }

    public static void dumpBlockStates() {
        for (int id : ((IIdList) Block.STATE_IDS).multiconnect_ids()) {
            BlockState state = Block.STATE_IDS.get(id);
            assert state != null;
            StringBuilder sb = new StringBuilder().append(id).append(": ").append(Registry.BLOCK.getId(state.getBlock()));
            if (!state.getEntries().isEmpty()) {
                sb.append("[")
                        .append(state.getEntries().entrySet().stream()
                                .sorted(Comparator.comparing(entry -> entry.getKey().getName()))
                                .map(entry -> entry.getKey().getName() + "=" + Util.getValueAsString(entry.getKey(), entry.getValue()))
                                .collect(Collectors.joining(",")))
                        .append("]");
            }
            System.out.println(sb);
        }
    }
}
