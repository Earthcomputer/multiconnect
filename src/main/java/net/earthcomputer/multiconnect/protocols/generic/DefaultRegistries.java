package net.earthcomputer.multiconnect.protocols.generic;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.*;
import net.earthcomputer.multiconnect.impl.Utils;
import net.earthcomputer.multiconnect.mixin.bridge.SpawnEggItemAccessor;
import net.earthcomputer.multiconnect.mixin.bridge.TrackedDataHandlerRegistryAccessor;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.collection.Int2ObjectBiMap;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;

import java.util.*;

public class DefaultRegistries<T> {
    private static boolean initialized = false;

    private static final Map<Block, Item> DEFAULT_BLOCK_ITEMS = new HashMap<>();
    private static final Map<EntityType<?>, SpawnEggItem> DEFAULT_SPAWN_EGG_ITEMS = new IdentityHashMap<>();
    private static final Int2ObjectBiMap<TrackedDataHandler<?>> DEFAULT_TRACKED_DATA_HANDLERS = Int2ObjectBiMap.create(16);

    public final ObjectList<T> defaultRawIdToEntry = new ObjectArrayList<>(256);
    public final Object2IntMap<T> defaultEntryToRawId = new Object2IntOpenCustomHashMap<>(Util.identityHashStrategy());
    public final BiMap<Identifier, T> defaultIdToEntry = HashBiMap.create();
    private final BiMap<RegistryKey<T>, T> defaultKeyToEntry = HashBiMap.create();
    private final Map<T, Lifecycle> defaultEntryToLifecycle = new IdentityHashMap<>();
    private final int defaultNextId;

    private DefaultRegistries(Registry<T> registry) {
        for (T t : registry) {
            int rawId = registry.getRawId(t);
            while (rawId >= defaultRawIdToEntry.size()) {
                defaultRawIdToEntry.add(null);
            }
            defaultRawIdToEntry.set(rawId, t);
            defaultEntryToRawId.put(t, rawId);
            defaultIdToEntry.put(registry.getId(t), t);
            assert registry.getKey(t).isPresent();
            defaultKeyToEntry.put(registry.getKey(t).get(), t);
            defaultEntryToLifecycle.put(t, ((ISimpleRegistry<?>) registry).getEntryToLifecycle().get(t));
        }
        defaultNextId = ((ISimpleRegistry<?>) registry).getNextId();
    }

    public void restore(SimpleRegistry<T> registry) {
        @SuppressWarnings("unchecked") ISimpleRegistry<T> iregistry = (ISimpleRegistry<T>) registry;

        List<T> added = new ArrayList<>();
        for (T thing : defaultRawIdToEntry) {
            if (!iregistry.getIdToEntry().containsValue(thing)) {
                added.add(thing);
            }
        }
        List<T> removed = new ArrayList<>();
        for (T thing : registry) {
            if (!defaultIdToEntry.containsValue(thing)) {
                removed.add(thing);
            }
        }

        iregistry.getRawIdToEntry().clear();
        iregistry.getRawIdToEntry().addAll(defaultRawIdToEntry);
        iregistry.getEntryToRawId().clear();
        iregistry.getEntryToRawId().putAll(defaultEntryToRawId);
        iregistry.getIdToEntry().clear();
        iregistry.getIdToEntry().putAll(defaultIdToEntry);
        iregistry.getKeyToEntry().clear();
        iregistry.getKeyToEntry().putAll(defaultKeyToEntry);
        iregistry.getEntryToLifecycle().clear();
        iregistry.getEntryToLifecycle().putAll(defaultEntryToLifecycle);
        iregistry.setRandomEntries(null);
        iregistry.setNextId(defaultNextId);
        iregistry.onRestore(added, removed);
    }

    public static Map<Registry<?>, DefaultRegistries<?>> DEFAULT_REGISTRIES = new LinkedHashMap<>();
    static {
        initialize();
    }

    @SuppressWarnings("unchecked")
    public static <T> void restore(Registry<?> registry, DefaultRegistries<?> defaultRegistries) {
        ((DefaultRegistries<T>) defaultRegistries).restore((SimpleRegistry<T>) registry);
    }

    public static void restoreAll() {
        DEFAULT_REGISTRIES.forEach((DefaultRegistries::restore));
        Item.BLOCK_ITEMS.clear();
        Item.BLOCK_ITEMS.putAll(DEFAULT_BLOCK_ITEMS);
        SpawnEggItemAccessor.getSpawnEggs().clear();
        SpawnEggItemAccessor.getSpawnEggs().putAll(DEFAULT_SPAWN_EGG_ITEMS);
        TrackedDataHandlerRegistryAccessor.getDataHandlers().clear();
        for (TrackedDataHandler<?> handler : DEFAULT_TRACKED_DATA_HANDLERS)
            TrackedDataHandlerRegistryAccessor.getDataHandlers().put(handler, DEFAULT_TRACKED_DATA_HANDLERS.getRawId(handler));
    }

    public static void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;

        DEFAULT_REGISTRIES.put(Registry.BLOCK, new DefaultRegistries<>(Registry.BLOCK));
        DEFAULT_REGISTRIES.put(Registry.ENTITY_TYPE, new DefaultRegistries<>(Registry.ENTITY_TYPE));
        DEFAULT_REGISTRIES.put(Registry.ITEM, new DefaultRegistries<>(Registry.ITEM));
        DEFAULT_REGISTRIES.put(Registry.ENCHANTMENT, new DefaultRegistries<>(Registry.ENCHANTMENT));
        DEFAULT_REGISTRIES.put(Registry.POTION, new DefaultRegistries<>(Registry.POTION));
        DEFAULT_REGISTRIES.put(Registry.PARTICLE_TYPE, new DefaultRegistries<>(Registry.PARTICLE_TYPE));
        DEFAULT_REGISTRIES.put(Registry.BLOCK_ENTITY_TYPE, new DefaultRegistries<>(Registry.BLOCK_ENTITY_TYPE));
        DEFAULT_REGISTRIES.put(Registry.SCREEN_HANDLER, new DefaultRegistries<>(Registry.SCREEN_HANDLER));
        DEFAULT_REGISTRIES.put(Registry.STATUS_EFFECT, new DefaultRegistries<>(Registry.STATUS_EFFECT));
        DEFAULT_REGISTRIES.put(Registry.RECIPE_SERIALIZER, new DefaultRegistries<>(Registry.RECIPE_SERIALIZER));
        DEFAULT_REGISTRIES.put(Registry.SOUND_EVENT, new DefaultRegistries<>(Registry.SOUND_EVENT));
        DEFAULT_REGISTRIES.put(Registry.CUSTOM_STAT, new DefaultRegistries<>(Registry.CUSTOM_STAT));

        DEFAULT_BLOCK_ITEMS.putAll(Item.BLOCK_ITEMS);
        DEFAULT_SPAWN_EGG_ITEMS.putAll(SpawnEggItemAccessor.getSpawnEggs());
        for (TrackedDataHandler<?> handler : TrackedDataHandlerRegistryAccessor.getDataHandlers())
            DEFAULT_TRACKED_DATA_HANDLERS.put(handler, TrackedDataHandlerRegistryAccessor.getDataHandlers().getRawId(handler));

        //noinspection unchecked
        ((ISimpleRegistry<Block>) Registry.BLOCK).addRegisterListener((block, inPlace) -> {
            if (DEFAULT_BLOCK_ITEMS.containsKey(block)) {
                Item item = DEFAULT_BLOCK_ITEMS.get(block);
                Item.BLOCK_ITEMS.put(block, item);
                //noinspection unchecked
                Utils.reregister((ISimpleRegistry<Item>) Registry.ITEM, item, inPlace);
            }
        });
        //noinspection unchecked
        ((ISimpleRegistry<Block>) Registry.BLOCK).addUnregisterListener((block, inPlace) -> {
            if (Item.BLOCK_ITEMS.containsKey(block)) {
                //noinspection unchecked
                ISimpleRegistry<Item> itemRegistry = (ISimpleRegistry<Item>) Registry.ITEM;
                Item item = Item.BLOCK_ITEMS.remove(block);
                if (inPlace) {
                    itemRegistry.purge(item);
                } else {
                    itemRegistry.unregister(item);
                }
            }
        });
        //noinspection unchecked
        ((ISimpleRegistry<EntityType<?>>) Registry.ENTITY_TYPE).addRegisterListener((entityType, inPlace) -> {
            if (DEFAULT_SPAWN_EGG_ITEMS.containsKey(entityType)) {
                SpawnEggItem item = DEFAULT_SPAWN_EGG_ITEMS.get(entityType);
                SpawnEggItemAccessor.getSpawnEggs().put(entityType, item);
                //noinspection unchecked
                Utils.reregister((ISimpleRegistry<Item>) Registry.ITEM, item, inPlace);
            }
        });
        //noinspection unchecked
        ((ISimpleRegistry<EntityType<?>>) Registry.ENTITY_TYPE).addUnregisterListener((entityType, inPlace) -> {
            if (SpawnEggItemAccessor.getSpawnEggs().containsKey(entityType)) {
                //noinspection unchecked
                ISimpleRegistry<Item> itemRegistry = (ISimpleRegistry<Item>) Registry.ITEM;
                SpawnEggItem spawnEgg = SpawnEggItemAccessor.getSpawnEggs().remove(entityType);
                if (inPlace) {
                    itemRegistry.purge(spawnEgg);
                } else {
                    itemRegistry.unregister(spawnEgg);
                }
            }
        });
    }
}
