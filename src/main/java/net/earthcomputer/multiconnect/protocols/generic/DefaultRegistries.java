package net.earthcomputer.multiconnect.protocols.generic;

import net.earthcomputer.multiconnect.mixin.bridge.SpawnEggItemAccessor;
import net.earthcomputer.multiconnect.mixin.bridge.TrackedDataHandlerRegistryAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.particle.ParticleType;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.Int2ObjectBiMap;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;

import java.util.*;

public class DefaultRegistries {
    private static boolean initialized = false;

    private static final Map<Block, Item> DEFAULT_BLOCK_ITEMS = new HashMap<>();
    private static final Map<EntityType<?>, SpawnEggItem> DEFAULT_SPAWN_EGG_ITEMS = new IdentityHashMap<>();
    private static final Int2ObjectBiMap<TrackedDataHandler<?>> DEFAULT_TRACKED_DATA_HANDLERS = Int2ObjectBiMap.create(16);

    private static final Map<RegistryKey<?>, RegistryBuilder<?>> DEFAULT_REGISTRY_BUILDERS = new LinkedHashMap<>();
    private static final Map<RegistryKey<?>, SimpleRegistry<?>> DEFAULT_REGISTRIES = new HashMap<>();
    static {
        initialize();
    }

    public static void restoreAll() {
        DEFAULT_REGISTRY_BUILDERS.values().forEach(RegistryBuilder::apply);
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

        DEFAULT_REGISTRY_BUILDERS.put(Registry.BLOCK_KEY, new RegistryBuilder<>(Registry.BLOCK));
        DEFAULT_REGISTRY_BUILDERS.put(Registry.ENTITY_TYPE_KEY, new RegistryBuilder<>(Registry.ENTITY_TYPE));
        DEFAULT_REGISTRY_BUILDERS.put(Registry.ITEM_KEY, new RegistryBuilder<>(Registry.ITEM));
        DEFAULT_REGISTRY_BUILDERS.put(Registry.ENCHANTMENT_KEY, new RegistryBuilder<>((SimpleRegistry<Enchantment>) Registry.ENCHANTMENT));
        DEFAULT_REGISTRY_BUILDERS.put(Registry.POTION_KEY, new RegistryBuilder<>(Registry.POTION));
        DEFAULT_REGISTRY_BUILDERS.put(Registry.PARTICLE_TYPE_KEY, new RegistryBuilder<>((SimpleRegistry<ParticleType<?>>) Registry.PARTICLE_TYPE));
        DEFAULT_REGISTRY_BUILDERS.put(Registry.BLOCK_ENTITY_TYPE_KEY, new RegistryBuilder<>((SimpleRegistry<BlockEntityType<?>>) Registry.BLOCK_ENTITY_TYPE));
        DEFAULT_REGISTRY_BUILDERS.put(Registry.MENU_KEY, new RegistryBuilder<>((SimpleRegistry<ScreenHandlerType<?>>) Registry.SCREEN_HANDLER));
        DEFAULT_REGISTRY_BUILDERS.put(Registry.MOB_EFFECT_KEY, new RegistryBuilder<>((SimpleRegistry<StatusEffect>) Registry.STATUS_EFFECT));
        DEFAULT_REGISTRY_BUILDERS.put(Registry.RECIPE_SERIALIZER_KEY, new RegistryBuilder<>((SimpleRegistry<RecipeSerializer<?>>) Registry.RECIPE_SERIALIZER));
        DEFAULT_REGISTRY_BUILDERS.put(Registry.SOUND_EVENT_KEY, new RegistryBuilder<>((SimpleRegistry<SoundEvent>) Registry.SOUND_EVENT));
        DEFAULT_REGISTRY_BUILDERS.put(Registry.CUSTOM_STAT_KEY, new RegistryBuilder<>((SimpleRegistry<Identifier>) Registry.CUSTOM_STAT));

        DEFAULT_REGISTRY_BUILDERS.forEach((k, v) -> DEFAULT_REGISTRIES.put(k, v.createCopiedRegistry()));

        DEFAULT_BLOCK_ITEMS.putAll(Item.BLOCK_ITEMS);
        DEFAULT_SPAWN_EGG_ITEMS.putAll(SpawnEggItemAccessor.getSpawnEggs());
        for (TrackedDataHandler<?> handler : TrackedDataHandlerRegistryAccessor.getDataHandlers())
            DEFAULT_TRACKED_DATA_HANDLERS.put(handler, TrackedDataHandlerRegistryAccessor.getDataHandlers().getRawId(handler));

        //noinspection unchecked
        ((ISimpleRegistry<Block>) Registry.BLOCK).multiconnect_addRegisterListener((block, inPlace, builderSupplier) -> {
            if (DEFAULT_BLOCK_ITEMS.containsKey(block)) {
                Item item = DEFAULT_BLOCK_ITEMS.get(block);
                Item.BLOCK_ITEMS.put(block, item);
                reregister(builderSupplier.get(Registry.ITEM_KEY), item, inPlace);
            }
        });
        //noinspection unchecked
        ((ISimpleRegistry<Block>) Registry.BLOCK).multiconnect_addUnregisterListener((block, inPlace, builderSupplier) -> {
            if (Item.BLOCK_ITEMS.containsKey(block)) {
                RegistryBuilder<Item> itemRegistry = builderSupplier.get(Registry.ITEM_KEY);
                Item item = Item.BLOCK_ITEMS.remove(block);
                if (inPlace) {
                    itemRegistry.purge(item);
                } else {
                    itemRegistry.unregister(item);
                }
            }
        });
        //noinspection unchecked
        ((ISimpleRegistry<EntityType<?>>) Registry.ENTITY_TYPE).multiconnect_addRegisterListener((entityType, inPlace, builderSupplier) -> {
            if (DEFAULT_SPAWN_EGG_ITEMS.containsKey(entityType)) {
                SpawnEggItem item = DEFAULT_SPAWN_EGG_ITEMS.get(entityType);
                SpawnEggItemAccessor.getSpawnEggs().put(entityType, item);
                reregister(builderSupplier.get(Registry.ITEM_KEY), item, inPlace);
            }
        });
        //noinspection unchecked
        ((ISimpleRegistry<EntityType<?>>) Registry.ENTITY_TYPE).multiconnect_addUnregisterListener((entityType, inPlace, builderSupplier) -> {
            if (SpawnEggItemAccessor.getSpawnEggs().containsKey(entityType)) {
                RegistryBuilder<Item> itemRegistry = builderSupplier.get(Registry.ITEM_KEY);
                SpawnEggItem spawnEgg = SpawnEggItemAccessor.getSpawnEggs().remove(entityType);
                if (inPlace) {
                    itemRegistry.purge(spawnEgg);
                } else {
                    itemRegistry.unregister(spawnEgg);
                }
            }
        });
    }

    private static <T> void reregister(RegistryBuilder<T> registry, T value, boolean inPlace) {
        if (registry.contains(value)) {
            return;
        }

        Registry<T> defaultRegistry = getDefaultRegistry(registry.getKey());
        int rawId = defaultRegistry.getRawId(value);
        for (int id = rawId - 1; id >= 0; id--) {
            RegistryEntry<T> entry = defaultRegistry.getEntry(id).orElse(null);
            if (entry != null && registry.contains(entry.value())) {
                Identifier identifier = defaultRegistry.getId(value);
                if (inPlace) {
                    registry.insertAfterInPlace(entry.value(), value, identifier);
                } else {
                    registry.insertAfter(entry.value(), value, identifier);
                }
                return;
            }
        }

        throw new IllegalStateException("Reregistering at 0");
    }

    @SuppressWarnings("unchecked")
    public static <T> Registry<T> getDefaultRegistry(RegistryKey<? extends Registry<T>> key) {
        return (Registry<T>) DEFAULT_REGISTRIES.get(key);
    }

    public static Collection<SimpleRegistry<?>> getDefaultRegistries() {
        return DEFAULT_REGISTRIES.values();
    }
}
