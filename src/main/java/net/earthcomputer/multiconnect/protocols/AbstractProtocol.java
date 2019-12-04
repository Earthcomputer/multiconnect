package net.earthcomputer.multiconnect.protocols;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.earthcomputer.multiconnect.impl.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.block.BlockColorProvider;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColorProvider;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.NetworkState;
import net.minecraft.network.Packet;
import net.minecraft.state.property.Property;
import net.minecraft.util.IdList;
import net.minecraft.util.Identifier;
import net.minecraft.util.Int2ObjectBiMap;
import net.minecraft.util.SystemUtil;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractProtocol {

    public void setup() {
        modifyPacketLists();
        DataTrackerManager.onConnectToServer();
        DefaultRegistry.restoreAll();
        DefaultRegistry.DEFAULT_REGISTRIES.keySet().forEach((registry -> modifyRegistry((ISimpleRegistry<?>) registry)));
        recomputeBlockStates();
        {
            IdList<BlockColorProvider> srcProviders = ((IBlockColors) BlockColors.create()).getProviders();
            IdList<BlockColorProvider> dstProviders = ((IBlockColors) MinecraftClient.getInstance().getBlockColorMap()).getProviders();
            ((IIdList) dstProviders).clear();
            for (int id : ((IIdList) srcProviders).ids()) {
                if (id != 0)
                    dstProviders.set(srcProviders.get(id), id);
            }
        }
        {
            IdList<ItemColorProvider> srcProviders = ((IItemColors) ItemColors.create(MinecraftClient.getInstance().getBlockColorMap())).getProviders();
            IdList<ItemColorProvider> dstProviders = ((IItemColors) ((IMinecraftClient) MinecraftClient.getInstance()).getItemColorMap()).getProviders();
            ((IIdList) dstProviders).clear();
            for (int id : ((IIdList) srcProviders).ids()) {
                if (id != 0)
                    dstProviders.set(srcProviders.get(id), id);
            }
        }
        ((IMinecraftClient) MinecraftClient.getInstance()).callInitializeSearchableContainers();
        ((IMinecraftClient) MinecraftClient.getInstance()).getSearchManager().apply(MinecraftClient.getInstance().getResourceManager());
    }

    protected void modifyPacketLists() {
        ((INetworkState) NetworkState.PLAY).getPacketHandlerMap().clear();

        for (Class<? extends Packet<?>> packet : getClientboundPackets()) {
            ((INetworkState) NetworkState.PLAY).multiconnect_addPacket(NetworkSide.CLIENTBOUND, packet);
        }
        for (Class<? extends Packet<?>> packet : getServerboundPackets()) {
            ((INetworkState) NetworkState.PLAY).multiconnect_addPacket(NetworkSide.SERVERBOUND, packet);
        }
    }

    protected static <T> void insertAfter(List<T> list, T element, T toInsert) {
        list.add(list.indexOf(element) + 1, toInsert);
    }

    @SuppressWarnings("unchecked")
    protected static <T> void insertAfter(ISimpleRegistry<T> registry, T element, T toInsert, String id) {
        registry.register(toInsert, ((SimpleRegistry<T>) registry).getRawId(element) + 1, new Identifier(id));
    }

    protected void recomputeBlockStates() {
        ((IIdList) Block.STATE_IDS).clear();
        for (Block block : Registry.BLOCK) {
            for (BlockState state : block.getStateFactory().getStates()) {
                if (acceptBlockState(state)) {
                    Block.STATE_IDS.add(state);
                }
            }
        }
    }

    public List<Class<? extends Packet<?>>> getClientboundPackets() {
        return new ArrayList<>(DefaultPackets.CLIENTBOUND);
    }

    public List<Class<? extends Packet<?>>> getServerboundPackets() {
        return new ArrayList<>(DefaultPackets.SERVERBOUND);
    }

    public void transformPacketClientbound(Class<? extends Packet<?>> packetClass, List<TransformerByteBuf> transformers) {
    }

    public void transformPacketServerbound(Class<? extends Packet<?>> packetClass, List<TransformerByteBuf> transformers) {
    }

    public boolean onSendPacket(Packet<?> packet) {
        return true;
    }

    public void modifyRegistry(ISimpleRegistry<?> registry) {
    }

    public boolean acceptBlockState(BlockState state) {
        return true;
    }

    public boolean acceptEntityData(Class<? extends Entity> clazz, TrackedData<?> data) {
        return true;
    }

    public void postEntityDataRegister(Class<? extends Entity> clazz) {
    }

    @SuppressWarnings("unchecked")
    public static <T> int getUnmodifiedId(Registry<T> registry, T value) {
        DefaultRegistry<T> defaultRegistry = (DefaultRegistry<T>) DefaultRegistry.DEFAULT_REGISTRIES.get(registry);
        if (defaultRegistry == null) return registry.getRawId(value);
        return defaultRegistry.defaultIndexedEntries.getId(value);
    }

    @SuppressWarnings("unchecked")
    public static <T> void rename(ISimpleRegistry<T> registry, T value, String newName) {
        int id = ((SimpleRegistry<T>) registry).getRawId(value);
        registry.unregister(value);
        registry.register(value, id, new Identifier(newName));
    }

    @SuppressWarnings("unchecked")
    public static <T> void reregister(ISimpleRegistry<T> registry, T value) {
        if (registry.getEntries().containsValue(value))
            return;

        //noinspection SuspiciousMethodCalls
        DefaultRegistry<T> defaultRegistry = (DefaultRegistry<T>) DefaultRegistry.DEFAULT_REGISTRIES.get(registry);
        T prevValue = null;
        for (int id = defaultRegistry.defaultIndexedEntries.getId(value) - 1; id >= 0; id--) {
            T val = defaultRegistry.defaultIndexedEntries.get(id);
            if (registry.getEntries().containsValue(val)) {
                prevValue = val;
                break;
            }
        }

        insertAfter(registry, prevValue, value, defaultRegistry.defaultEntries.inverse().get(value).toString());
    }

    protected static void dumpBlockStates() {
        for (int id : ((IIdList) Block.STATE_IDS).ids()) {
            BlockState state = Block.STATE_IDS.get(id);
            assert state != null;
            StringBuilder sb = new StringBuilder().append(id).append(": ").append(Registry.BLOCK.getId(state.getBlock()));
            if (!state.getProperties().isEmpty()) {
                sb.append("[")
                        .append(state.getProperties().stream()
                                .sorted(Comparator.comparing(Property::getName))
                                .map(p -> p.getName() + "=" + SystemUtil.getValueAsString(p, state.get(p)))
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
        private static List<Class<? extends Packet<?>>> CLIENTBOUND = new ArrayList<>();
        private static List<Class<? extends Packet<?>>> SERVERBOUND = new ArrayList<>();

        private static void initialize() {
            Map<NetworkSide, BiMap<Integer, Class<? extends Packet<?>>>> packetHandlerMap = ((INetworkState) NetworkState.PLAY).getPacketHandlerMap();
            BiMap<Integer, Class<? extends Packet<?>>> clientPacketMap = packetHandlerMap.get(NetworkSide.CLIENTBOUND);
            CLIENTBOUND.addAll(clientPacketMap.values());
            CLIENTBOUND.sort(Comparator.comparing(packet -> clientPacketMap.inverse().get(packet)));
            BiMap<Integer, Class<? extends Packet<?>>> serverPacketMap = packetHandlerMap.get(NetworkSide.SERVERBOUND);
            SERVERBOUND.addAll(serverPacketMap.values());
            SERVERBOUND.sort(Comparator.comparing(packet -> serverPacketMap.inverse().get(packet)));
        }
    }

    private static class DefaultRegistry<T> {

        private static Map<Block, Item> DEFAULT_BLOCK_ITEMS = new HashMap<>();
        private static Map<EntityType<?>, SpawnEggItem> DEFAULT_SPAWN_EGG_ITEMS = new IdentityHashMap<>();

        private Int2ObjectBiMap<T> defaultIndexedEntries = new Int2ObjectBiMap<>(256);
        private BiMap<Identifier, T> defaultEntries = HashBiMap.create();
        private int defaultNextId;

        private DefaultRegistry(Registry<T> registry) {
            for (T t : registry) {
                defaultIndexedEntries.put(t, registry.getRawId(t));
                defaultEntries.put(registry.getId(t), t);
            }
            defaultNextId = ((ISimpleRegistry) registry).getNextId();
        }

        public void restore(SimpleRegistry<T> registry) {
            @SuppressWarnings("unchecked") ISimpleRegistry<T> iregistry = (ISimpleRegistry<T>) registry;
            iregistry.getIndexedEntries().clear();
            defaultIndexedEntries.iterator().forEachRemaining(t -> iregistry.getIndexedEntries().put(t, defaultIndexedEntries.getId(t)));
            iregistry.getEntries().clear();
            iregistry.getEntries().putAll(defaultEntries);
            iregistry.setNextId(defaultNextId);
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
            getSpawnEggItems().clear();
            getSpawnEggItems().putAll(DEFAULT_SPAWN_EGG_ITEMS);
        }

        public static void initialize() {
            DEFAULT_REGISTRIES.put(Registry.BLOCK, new DefaultRegistry<>(Registry.BLOCK));
            DEFAULT_REGISTRIES.put(Registry.ENTITY_TYPE, new DefaultRegistry<>(Registry.ENTITY_TYPE));
            DEFAULT_REGISTRIES.put(Registry.ITEM, new DefaultRegistry<>(Registry.ITEM));
            DEFAULT_REGISTRIES.put(Registry.ENCHANTMENT, new DefaultRegistry<>(Registry.ENCHANTMENT));
            DEFAULT_REGISTRIES.put(Registry.POTION, new DefaultRegistry<>(Registry.POTION));
            DEFAULT_REGISTRIES.put(Registry.BIOME, new DefaultRegistry<>(Registry.BIOME));
            DEFAULT_REGISTRIES.put(Registry.PARTICLE_TYPE, new DefaultRegistry<>(Registry.PARTICLE_TYPE));
            DEFAULT_REGISTRIES.put(Registry.BLOCK_ENTITY, new DefaultRegistry<>(Registry.BLOCK_ENTITY));
            DEFAULT_REGISTRIES.put(Registry.CONTAINER, new DefaultRegistry<>(Registry.CONTAINER));
            DEFAULT_REGISTRIES.put(Registry.STATUS_EFFECT, new DefaultRegistry<>(Registry.STATUS_EFFECT));
            DEFAULT_REGISTRIES.put(Registry.RECIPE_SERIALIZER, new DefaultRegistry<>(Registry.RECIPE_SERIALIZER));
            DEFAULT_REGISTRIES.put(Registry.SOUND_EVENT, new DefaultRegistry<>(Registry.SOUND_EVENT));

            DEFAULT_BLOCK_ITEMS.putAll(Item.BLOCK_ITEMS);
            DEFAULT_SPAWN_EGG_ITEMS.putAll(getSpawnEggItems());

            //noinspection unchecked
            ((ISimpleRegistry<Block>) Registry.BLOCK).addRegisterListener(block -> {
                if (DEFAULT_BLOCK_ITEMS.containsKey(block)) {
                    Item item = DEFAULT_BLOCK_ITEMS.get(block);
                    Item.BLOCK_ITEMS.put(block, item);
                    //noinspection unchecked
                    reregister((ISimpleRegistry<Item>) Registry.ITEM, item);
                }
            });
            //noinspection unchecked
            ((ISimpleRegistry<Block>) Registry.BLOCK).addUnregisterListener(block -> {
                if (Item.BLOCK_ITEMS.containsKey(block)) {
                    //noinspection unchecked
                    ((ISimpleRegistry<Item>) Registry.ITEM).unregister(Item.BLOCK_ITEMS.remove(block));
                }
            });
            //noinspection unchecked
            ((ISimpleRegistry<EntityType<?>>) Registry.ENTITY_TYPE).addRegisterListener(entityType -> {
                if (DEFAULT_SPAWN_EGG_ITEMS.containsKey(entityType)) {
                    SpawnEggItem item = DEFAULT_SPAWN_EGG_ITEMS.get(entityType);
                    //noinspection unchecked
                    reregister((ISimpleRegistry<Item>) Registry.ITEM, item);
                }
            });
            //noinspection unchecked
            ((ISimpleRegistry<EntityType<?>>) Registry.ENTITY_TYPE).addUnregisterListener(entityType -> {
                if (getSpawnEggItems().containsKey(entityType)) {
                    //noinspection unchecked
                    ((ISimpleRegistry<Item>) Registry.ITEM).unregister(getSpawnEggItems().remove(entityType));
                }
            });
        }

        private static Map<EntityType<?>, SpawnEggItem> getSpawnEggItems() {
            try {
                //noinspection unchecked
                return (Map<EntityType<?>, SpawnEggItem>) SPAWN_EGGS_FIELD.get(null);
            } catch (ReflectiveOperationException e) {
                throw new AssertionError(e);
            }
        }

        private static final Field SPAWN_EGGS_FIELD;
        static {
            try {
                SPAWN_EGGS_FIELD = Arrays.stream(SpawnEggItem.class.getDeclaredFields())
                        .filter(it -> it.getType() == Map.class)
                        .findFirst().orElseThrow(NoSuchFieldException::new);
                SPAWN_EGGS_FIELD.setAccessible(true);
            } catch (ReflectiveOperationException e) {
                throw new AssertionError(e);
            }
        }
    }

}
