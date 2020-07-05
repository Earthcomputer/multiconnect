package net.earthcomputer.multiconnect.protocols.generic;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.earthcomputer.multiconnect.impl.Utils;
import net.earthcomputer.multiconnect.mixin.bridge.SpawnEggItemAccessor;
import net.earthcomputer.multiconnect.mixin.bridge.TrackedDataHandlerRegistryAccessor;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.Int2ObjectBiMap;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;

import java.util.*;

public class DefaultRegistries<T> {
    private static Map<Block, Item> DEFAULT_BLOCK_ITEMS = new HashMap<>();
    private static Map<EntityType<?>, SpawnEggItem> DEFAULT_SPAWN_EGG_ITEMS = new IdentityHashMap<>();
    private static Int2ObjectBiMap<TrackedDataHandler<?>> DEFAULT_TRACKED_DATA_HANDLERS = new Int2ObjectBiMap<>(16);

    public Int2ObjectBiMap<T> defaultIndexedEntries = new Int2ObjectBiMap<>(256);
    public BiMap<Identifier, T> defaultEntriesById = HashBiMap.create();
    private BiMap<RegistryKey, T> defaultEntriesByKey = HashBiMap.create();
    private int defaultNextId;

    private DefaultRegistries(Registry<T> registry) {
        for (T t : registry) {
            defaultIndexedEntries.put(t, registry.getRawId(t));
            defaultEntriesById.put(registry.getId(t), t);
            assert registry.getKey(t).isPresent();
            defaultEntriesByKey.put(registry.getKey(t).get(), t);
        }
        defaultNextId = ((ISimpleRegistry) registry).getNextId();
    }

    public void restore(SimpleRegistry<T> registry) {
        List<T> added = new ArrayList<>();
        for (T thing : defaultIndexedEntries) {
            if (!registry.containsId(defaultIndexedEntries.getId(thing))) {
                added.add(thing);
            }
        }
        List<T> removed = new ArrayList<>();
        for (T thing : registry) {
            if (!defaultEntriesById.containsValue(thing)) {
                removed.add(thing);
            }
        }

        @SuppressWarnings("unchecked") ISimpleRegistry<T> iregistry = (ISimpleRegistry<T>) registry;
        iregistry.getIndexedEntries().clear();
        defaultIndexedEntries.iterator().forEachRemaining(t -> iregistry.getIndexedEntries().put(t, defaultIndexedEntries.getId(t)));
        iregistry.getEntriesById().clear();
        iregistry.getEntriesById().putAll(defaultEntriesById);
        iregistry.getEntriesByKey().clear();
        iregistry.getEntriesByKey().putAll(defaultEntriesByKey);
        iregistry.setNextId(defaultNextId);
        iregistry.onRestore(added, removed);
    }

    public static Map<Registry<?>, DefaultRegistries<?>> DEFAULT_REGISTRIES = new LinkedHashMap<>();

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
        TrackedDataHandlerRegistryAccessor.getHandlers().clear();
        for (TrackedDataHandler<?> handler : DEFAULT_TRACKED_DATA_HANDLERS)
            TrackedDataHandlerRegistryAccessor.getHandlers().put(handler, DEFAULT_TRACKED_DATA_HANDLERS.getId(handler));
    }

    public static void initialize() {
        DEFAULT_REGISTRIES.put(Registry.BLOCK, new DefaultRegistries<>(Registry.BLOCK));
        DEFAULT_REGISTRIES.put(Registry.ENTITY_TYPE, new DefaultRegistries<>(Registry.ENTITY_TYPE));
        DEFAULT_REGISTRIES.put(Registry.ITEM, new DefaultRegistries<>(Registry.ITEM));
        DEFAULT_REGISTRIES.put(Registry.ENCHANTMENT, new DefaultRegistries<>(Registry.ENCHANTMENT));
        DEFAULT_REGISTRIES.put(Registry.POTION, new DefaultRegistries<>(Registry.POTION));
        DEFAULT_REGISTRIES.put(Registry.BIOME, new DefaultRegistries<>(Registry.BIOME));
        DEFAULT_REGISTRIES.put(Registry.PARTICLE_TYPE, new DefaultRegistries<>(Registry.PARTICLE_TYPE));
        DEFAULT_REGISTRIES.put(Registry.BLOCK_ENTITY_TYPE, new DefaultRegistries<>(Registry.BLOCK_ENTITY_TYPE));
        DEFAULT_REGISTRIES.put(Registry.SCREEN_HANDLER, new DefaultRegistries<>(Registry.SCREEN_HANDLER));
        DEFAULT_REGISTRIES.put(Registry.STATUS_EFFECT, new DefaultRegistries<>(Registry.STATUS_EFFECT));
        DEFAULT_REGISTRIES.put(Registry.RECIPE_SERIALIZER, new DefaultRegistries<>(Registry.RECIPE_SERIALIZER));
        DEFAULT_REGISTRIES.put(Registry.SOUND_EVENT, new DefaultRegistries<>(Registry.SOUND_EVENT));
        DEFAULT_REGISTRIES.put(Registry.CUSTOM_STAT, new DefaultRegistries<>(Registry.CUSTOM_STAT));

        DEFAULT_BLOCK_ITEMS.putAll(Item.BLOCK_ITEMS);
        DEFAULT_SPAWN_EGG_ITEMS.putAll(SpawnEggItemAccessor.getSpawnEggs());
        for (TrackedDataHandler<?> handler : TrackedDataHandlerRegistryAccessor.getHandlers())
            DEFAULT_TRACKED_DATA_HANDLERS.put(handler, TrackedDataHandlerRegistryAccessor.getHandlers().getId(handler));

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
