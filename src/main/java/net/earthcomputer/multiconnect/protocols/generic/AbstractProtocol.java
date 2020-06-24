package net.earthcomputer.multiconnect.protocols.generic;

import com.google.common.collect.BiMap;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashBiMap;
import net.earthcomputer.multiconnect.mixin.bridge.MinecraftClientAccessor;
import net.earthcomputer.multiconnect.mixin.bridge.SpawnEggItemAccessor;
import net.earthcomputer.multiconnect.mixin.bridge.TrackedDataHandlerRegistryAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.NetworkState;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.collection.Int2ObjectBiMap;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class AbstractProtocol {

    public void setup(boolean resourceReload) {
        if (!resourceReload) {
            modifyPacketLists();
            DataTrackerManager.onConnectToServer();
        }
        DefaultRegistry.restoreAll();
        RegistryMutator mutator = new RegistryMutator();
        mutateRegistries(mutator);
        mutator.runMutations(DefaultRegistry.DEFAULT_REGISTRIES.keySet());
        DefaultRegistry.DEFAULT_REGISTRIES.keySet().forEach((this::postMutateRegistry));
        recomputeBlockStates();
        if (!resourceReload) {
            removeTrackedDataHandlers();
            OldLanguageManager.reloadLanguages();
        }
        ((MinecraftClientAccessor) MinecraftClient.getInstance()).callInitializeSearchableContainers();
        ((MinecraftClientAccessor) MinecraftClient.getInstance()).getSearchManager().apply(MinecraftClient.getInstance().getResourceManager());
    }

    protected void modifyPacketLists() {
        INetworkState networkState = (INetworkState) (Object) NetworkState.PLAY;
        networkState.getPacketHandlers().values().forEach(IPacketHandler::multiconnect_clear);

        for (PacketInfo<?> packetInfo : getClientboundPackets()) {
            doRegister(networkState.getPacketHandlers().get(NetworkSide.CLIENTBOUND), packetInfo.getPacketClass(), packetInfo.getFactory());
            networkState.multiconnect_onAddPacket(packetInfo.getPacketClass());
        }
        for (PacketInfo<?> packetInfo : getServerboundPackets()) {
            doRegister(networkState.getPacketHandlers().get(NetworkSide.SERVERBOUND), packetInfo.getPacketClass(), packetInfo.getFactory());
            networkState.multiconnect_onAddPacket(packetInfo.getPacketClass());
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends PacketListener, P extends Packet<T>> void doRegister(IPacketHandler<T> handler, Class<?> packetClass, Supplier<?> factory) {
        handler.multiconnect_register((Class<P>) packetClass, (Supplier<P>) factory);
    }

    protected static void insertAfter(List<PacketInfo<?>> list, Class<? extends Packet<?>> element, PacketInfo<?> toInsert) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getPacketClass() == element) {
                list.add(i + 1, toInsert);
                return;
            }
        }
        list.add(0, toInsert);
    }

    protected static <T> void insertAfter(List<T> list, T element, T toInsert) {
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

    protected static void remove(List<PacketInfo<?>> list, Class<? extends Packet<?>> element) {
        list.removeIf(it -> it.getPacketClass() == element);
    }

    protected void recomputeBlockStates() {
        ((IIdList) Block.STATE_IDS).multiconnect_clear();
        for (Block block : Registry.BLOCK) {
            recomputeStatesForBlock(block);
        }
    }

    protected void recomputeStatesForBlock(Block block) {
        for (BlockState state : block.getStateManager().getStates()) {
            if (acceptBlockState(state)) {
                Block.STATE_IDS.add(state);
            }
        }
    }

    public List<PacketInfo<?>> getClientboundPackets() {
        return new ArrayList<>(DefaultPackets.CLIENTBOUND);
    }

    public List<PacketInfo<?>> getServerboundPackets() {
        return new ArrayList<>(DefaultPackets.SERVERBOUND);
    }

    public boolean onSendPacket(Packet<?> packet) {
        return true;
    }

    public void mutateRegistries(RegistryMutator mutator) {
    }

    @SuppressWarnings("unchecked")
    protected <T> void postMutateRegistry(Registry<T> registry) {
        if (!(registry instanceof SimpleRegistry)) return;
        if (registry instanceof DefaultedRegistry) return;
        ISimpleRegistry<T> iregistry = (ISimpleRegistry<T>) registry;
        DefaultRegistry<T> defaultRegistry = (DefaultRegistry<T>) DefaultRegistry.DEFAULT_REGISTRIES.get(registry);
        if (defaultRegistry == null) return;
        for (Map.Entry<Identifier, T> entry : defaultRegistry.defaultEntriesById.entrySet()) {
            if (registry.getId(entry.getValue()) == null) {
                RegistryKey<T> key = RegistryKey.of(iregistry.getRegistryKey(), entry.getKey());
                iregistry.register(entry.getValue(), iregistry.getNextId(), key, false);
            }
        }
    }

    public boolean acceptBlockState(BlockState state) {
        return true;
    }

    public boolean acceptEntityData(Class<? extends Entity> clazz, TrackedData<?> data) {
        return true;
    }

    public void postEntityDataRegister(Class<? extends Entity> clazz) {
    }

    public <T> T readTrackedData(TrackedDataHandler<T> handler, PacketByteBuf buf) {
        return handler.read(buf);
    }

    protected void removeTrackedDataHandlers() {
    }

    protected static void removeTrackedDataHandler(TrackedDataHandler<?> handler) {
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

    public void addExtraBlockTags(TagRegistry<Block> tags) {
    }

    public void addExtraItemTags(TagRegistry<Item> tags, TagRegistry<Block> blockTags) {
    }

    public void addExtraFluidTags(TagRegistry<Fluid> tags) {
    }

    public void addExtraEntityTags(TagRegistry<EntityType<?>> tags) {
    }

    protected void copyBlocks(TagRegistry<Item> tags, TagRegistry<Block> blockTags, Tag.Identified<Item> tag, Tag.Identified<Block> blockTag) {
        tags.add(tag, Collections2.transform(blockTags.get(blockTag.getId()), Block::asItem));
    }

    public boolean shouldBlockChangeReplaceBlockEntity(Block oldBlock, Block newBlock) {
        return oldBlock != newBlock;
    }

    @SuppressWarnings("unchecked")
    public static <T> int getUnmodifiedId(Registry<T> registry, T value) {
        DefaultRegistry<T> defaultRegistry = (DefaultRegistry<T>) DefaultRegistry.DEFAULT_REGISTRIES.get(registry);
        if (defaultRegistry == null) return registry.getRawId(value);
        return defaultRegistry.defaultIndexedEntries.getId(value);
    }

    @SuppressWarnings("unchecked")
    public static <T> Identifier getUnmodifiedName(Registry<T> registry, T value) {
        DefaultRegistry<T> defaultRegistry = (DefaultRegistry<T>) DefaultRegistry.DEFAULT_REGISTRIES.get(registry);
        if (defaultRegistry == null) return registry.getId(value);
        return defaultRegistry.defaultEntriesById.inverse().get(value);
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
        DefaultRegistry<T> defaultRegistry = (DefaultRegistry<T>) DefaultRegistry.DEFAULT_REGISTRIES.get(registry);
        T prevValue = null;
        for (int id = defaultRegistry.defaultIndexedEntries.getId(value) - 1; id >= 0; id--) {
            T val = defaultRegistry.defaultIndexedEntries.get(id);
            if (registry.getEntriesById().containsValue(val)) {
                prevValue = val;
                break;
            }
        }

        insertAfter(registry, prevValue, value, defaultRegistry.defaultEntriesById.inverse().get(value).toString(), inPlace);
    }

    protected static void dumpBlockStates() {
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

    static {
        DefaultPackets.initialize();
        DefaultRegistry.initialize();
    }

    private static class DefaultPackets {
        private static List<PacketInfo<?>> CLIENTBOUND = new ArrayList<>();
        private static List<PacketInfo<?>> SERVERBOUND = new ArrayList<>();

        private static void initialize() {
            Map<NetworkSide, ? extends IPacketHandler<?>> packetHandlerMap = ((INetworkState) (Object) NetworkState.PLAY).getPacketHandlers();
            IPacketHandler<?> clientPacketMap = packetHandlerMap.get(NetworkSide.CLIENTBOUND);
            CLIENTBOUND.addAll(clientPacketMap.multiconnect_values());
            IPacketHandler<?> serverPacketMap = packetHandlerMap.get(NetworkSide.SERVERBOUND);
            SERVERBOUND.addAll(serverPacketMap.multiconnect_values());
        }
    }

    private static class DefaultRegistry<T> {

        private static Map<Block, Item> DEFAULT_BLOCK_ITEMS = new HashMap<>();
        private static Map<EntityType<?>, SpawnEggItem> DEFAULT_SPAWN_EGG_ITEMS = new IdentityHashMap<>();
        private static Int2ObjectBiMap<TrackedDataHandler<?>> DEFAULT_TRACKED_DATA_HANDLERS = new Int2ObjectBiMap<>(16);

        private Int2ObjectBiMap<T> defaultIndexedEntries = new Int2ObjectBiMap<>(256);
        private BiMap<Identifier, T> defaultEntriesById = HashBiMap.create();
        private BiMap<RegistryKey, T> defaultEntriesByKey = HashBiMap.create();
        private int defaultNextId;

        private DefaultRegistry(Registry<T> registry) {
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

        public static Map<Registry<?>, DefaultRegistry<?>> DEFAULT_REGISTRIES = new LinkedHashMap<>();

        @SuppressWarnings("unchecked")
        public static <T> void restore(Registry<?> registry, DefaultRegistry<?> defaultRegistry) {
            ((DefaultRegistry<T>) defaultRegistry).restore((SimpleRegistry<T>) registry);
        }

        public static void restoreAll() {
            DEFAULT_REGISTRIES.forEach((DefaultRegistry::restore));
            Item.BLOCK_ITEMS.clear();
            Item.BLOCK_ITEMS.putAll(DEFAULT_BLOCK_ITEMS);
            SpawnEggItemAccessor.getSpawnEggs().clear();
            SpawnEggItemAccessor.getSpawnEggs().putAll(DEFAULT_SPAWN_EGG_ITEMS);
            TrackedDataHandlerRegistryAccessor.getHandlers().clear();
            for (TrackedDataHandler<?> handler : DEFAULT_TRACKED_DATA_HANDLERS)
                TrackedDataHandlerRegistryAccessor.getHandlers().put(handler, DEFAULT_TRACKED_DATA_HANDLERS.getId(handler));
        }

        public static void initialize() {
            DEFAULT_REGISTRIES.put(Registry.BLOCK, new DefaultRegistry<>(Registry.BLOCK));
            DEFAULT_REGISTRIES.put(Registry.ENTITY_TYPE, new DefaultRegistry<>(Registry.ENTITY_TYPE));
            DEFAULT_REGISTRIES.put(Registry.ITEM, new DefaultRegistry<>(Registry.ITEM));
            DEFAULT_REGISTRIES.put(Registry.ENCHANTMENT, new DefaultRegistry<>(Registry.ENCHANTMENT));
            DEFAULT_REGISTRIES.put(Registry.POTION, new DefaultRegistry<>(Registry.POTION));
            DEFAULT_REGISTRIES.put(Registry.BIOME, new DefaultRegistry<>(Registry.BIOME));
            DEFAULT_REGISTRIES.put(Registry.PARTICLE_TYPE, new DefaultRegistry<>(Registry.PARTICLE_TYPE));
            DEFAULT_REGISTRIES.put(Registry.BLOCK_ENTITY_TYPE, new DefaultRegistry<>(Registry.BLOCK_ENTITY_TYPE));
            DEFAULT_REGISTRIES.put(Registry.SCREEN_HANDLER, new DefaultRegistry<>(Registry.SCREEN_HANDLER));
            DEFAULT_REGISTRIES.put(Registry.STATUS_EFFECT, new DefaultRegistry<>(Registry.STATUS_EFFECT));
            DEFAULT_REGISTRIES.put(Registry.RECIPE_SERIALIZER, new DefaultRegistry<>(Registry.RECIPE_SERIALIZER));
            DEFAULT_REGISTRIES.put(Registry.SOUND_EVENT, new DefaultRegistry<>(Registry.SOUND_EVENT));
            DEFAULT_REGISTRIES.put(Registry.CUSTOM_STAT, new DefaultRegistry<>(Registry.CUSTOM_STAT));

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
                    reregister((ISimpleRegistry<Item>) Registry.ITEM, item, inPlace);
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
                    reregister((ISimpleRegistry<Item>) Registry.ITEM, item, inPlace);
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

}
