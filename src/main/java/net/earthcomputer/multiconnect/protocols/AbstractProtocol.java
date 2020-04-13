package net.earthcomputer.multiconnect.protocols;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.earthcomputer.multiconnect.impl.*;
import net.earthcomputer.multiconnect.mixin.SpawnEggItemAccessor;
import net.earthcomputer.multiconnect.mixin.DataSerializersAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.network.*;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.IDataSerializer;
import net.minecraft.state.IProperty;
import net.minecraft.util.IntIdentityHashBiMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.Registry;
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
        DefaultRegistry.DEFAULT_REGISTRIES.keySet().forEach((registry -> {
            modifyRegistry((ISimpleRegistry<?>) registry);
            postModifyRegistry(registry);
        }));
        recomputeBlockStates();
        if (!resourceReload) {
            removeIDataSerializers();
            Minecraft.getInstance().getLanguageManager().onResourceManagerReload(Minecraft.getInstance().getResourceManager());
        }
        ((IMinecraftClient) Minecraft.getInstance()).callPopulateSearchTreeManager();
        ((IMinecraftClient) Minecraft.getInstance()).getSearchTreeManager().onResourceManagerReload(Minecraft.getInstance().getResourceManager());
    }

    protected void modifyPacketLists() {
        IProtocolType networkState = (IProtocolType) (Object) ProtocolType.PLAY;
        networkState.getField_229711_h_().values().forEach(IPacketHandler::multiconnect_clear);

        for (PacketInfo<?> packetInfo : getClientboundPackets()) {
            doRegister(networkState.getField_229711_h_().get(PacketDirection.CLIENTBOUND), packetInfo.getPacketClass(), packetInfo.getFactory());
            networkState.multiconnect_onAddPacket(packetInfo.getPacketClass());
        }
        for (PacketInfo<?> packetInfo : getServerboundPackets()) {
            doRegister(networkState.getField_229711_h_().get(PacketDirection.SERVERBOUND), packetInfo.getPacketClass(), packetInfo.getFactory());
            networkState.multiconnect_onAddPacket(packetInfo.getPacketClass());
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends INetHandler, P extends IPacket<T>> void doRegister(IPacketHandler<T> handler, Class<?> packetClass, Supplier<?> factory) {
        handler.multiconnect_register((Class<P>) packetClass, (Supplier<P>) factory);
    }

    protected static void insertAfter(List<PacketInfo<?>> list, Class<? extends IPacket<?>> element, PacketInfo<?> toInsert) {
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

    @SuppressWarnings("unchecked")
    public static <T> void insertAfter(ISimpleRegistry<T> registry, T element, T toInsert, String id) {
        registry.register(toInsert, ((SimpleRegistry<T>) registry).getId(element) + 1, new ResourceLocation(id));
    }

    protected static void remove(List<PacketInfo<?>> list, Class<? extends IPacket<?>> element) {
        list.removeIf(it -> it.getPacketClass() == element);
    }

    protected void recomputeBlockStates() {
        ((IIdList) Block.BLOCK_STATE_IDS).multiconnect_clear();
        for (Block block : Registry.BLOCK) {
            for (BlockState state : block.getStateContainer().getValidStates()) {
                if (acceptBlockState(state)) {
                    Block.BLOCK_STATE_IDS.add(state);
                }
            }
        }
    }

    public List<PacketInfo<?>> getClientboundPackets() {
        return new ArrayList<>(DefaultPackets.CLIENTBOUND);
    }

    public List<PacketInfo<?>> getServerboundPackets() {
        return new ArrayList<>(DefaultPackets.SERVERBOUND);
    }

    public boolean onSendPacket(IPacket<?> packet) {
        return true;
    }

    public void modifyRegistry(ISimpleRegistry<?> registry) {
    }

    @SuppressWarnings("unchecked")
    protected <T> void postModifyRegistry(Registry<T> registry) {
        if (!(registry instanceof SimpleRegistry)) return;
        if (registry instanceof DefaultedRegistry) return;
        ISimpleRegistry<T> iregistry = (ISimpleRegistry<T>) registry;
        DefaultRegistry<T> defaultRegistry = (DefaultRegistry<T>) DefaultRegistry.DEFAULT_REGISTRIES.get(registry);
        if (defaultRegistry == null) return;
        for (Map.Entry<ResourceLocation, T> entry : defaultRegistry.defaultEntries.entrySet()) {
            if (registry.getKey(entry.getValue()) == null)
                iregistry.register(entry.getValue(), iregistry.getNextFreeId(), entry.getKey(), false);
        }
    }

    public boolean acceptBlockState(BlockState state) {
        return true;
    }

    public boolean acceptEntityData(Class<? extends Entity> clazz, DataParameter<?> data) {
        return true;
    }

    public void postEntityDataRegister(Class<? extends Entity> clazz) {
    }

    public <T> T readDataParameter(IDataSerializer<T> handler, PacketBuffer buf) {
        return handler.read(buf);
    }

    protected void removeIDataSerializers() {
    }

    protected static void removeIDataSerializer(IDataSerializer<?> handler) {
        IntIdentityHashBiMap<IDataSerializer<?>> biMap = DataSerializersAccessor.getHandlers();
        //noinspection unchecked
        IIntIdentityHashBiMap<IDataSerializer<?>> iBiMap = (IIntIdentityHashBiMap<IDataSerializer<?>>) biMap;
        int id = DataSerializers.getSerializerId(handler);
        iBiMap.multiconnect_remove(handler);
        for (; DataSerializers.getSerializer(id + 1) != null; id++) {
            IDataSerializer<?> h = DataSerializers.getSerializer(id + 1);
            iBiMap.multiconnect_remove(h);
            biMap.put(h, id);
        }
    }

    public boolean shouldBlockChangeReplaceBlockEntity(Block oldBlock, Block newBlock) {
        return oldBlock != newBlock;
    }

    @SuppressWarnings("unchecked")
    public static <T> int getUnmodifiedId(Registry<T> registry, T value) {
        DefaultRegistry<T> defaultRegistry = (DefaultRegistry<T>) DefaultRegistry.DEFAULT_REGISTRIES.get(registry);
        if (defaultRegistry == null) return registry.getId(value);
        return defaultRegistry.defaultIndexedEntries.getId(value);
    }

    @SuppressWarnings("unchecked")
    public static <T> ResourceLocation getUnmodifiedName(Registry<T> registry, T value) {
        DefaultRegistry<T> defaultRegistry = (DefaultRegistry<T>) DefaultRegistry.DEFAULT_REGISTRIES.get(registry);
        if (defaultRegistry == null) return registry.getKey(value);
        return defaultRegistry.defaultEntries.inverse().get(value);
    }

    @SuppressWarnings("unchecked")
    public static <T> void rename(ISimpleRegistry<T> registry, T value, String newName) {
        int id = ((SimpleRegistry<T>) registry).getId(value);
        registry.purge(value);
        registry.registerInPlace(value, id, new ResourceLocation(newName));
    }

    @SuppressWarnings("unchecked")
    public static <T> void reregister(ISimpleRegistry<T> registry, T value) {
        if (registry.getRegistryObjects().containsValue(value))
            return;

        //noinspection SuspiciousMethodCalls
        DefaultRegistry<T> defaultRegistry = (DefaultRegistry<T>) DefaultRegistry.DEFAULT_REGISTRIES.get(registry);
        T prevValue = null;
        for (int id = defaultRegistry.defaultIndexedEntries.getId(value) - 1; id >= 0; id--) {
            T val = defaultRegistry.defaultIndexedEntries.getByValue(id);
            if (registry.getRegistryObjects().containsValue(val)) {
                prevValue = val;
                break;
            }
        }

        insertAfter(registry, prevValue, value, defaultRegistry.defaultEntries.inverse().get(value).toString());
    }

    protected static void dumpBlockStates() {
        for (int id : ((IIdList) Block.BLOCK_STATE_IDS).multiconnect_ids()) {
            BlockState state = Block.BLOCK_STATE_IDS.getByValue(id);
            assert state != null;
            StringBuilder sb = new StringBuilder().append(id).append(": ").append(Registry.BLOCK.getId(state.getBlock()));
            if (!state.getProperties().isEmpty()) {
                sb.append("[")
                        .append(state.getProperties().stream()
                                .sorted(Comparator.comparing(IProperty::getName))
                                .map(p -> p.getName() + "=" + Util.getValueName(p, state.get(p)))
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
            Map<PacketDirection, ? extends IPacketHandler<?>> packetHandlerMap = ((IProtocolType) (Object) ProtocolType.PLAY).getField_229711_h_();
            IPacketHandler<?> clientPacketMap = packetHandlerMap.get(PacketDirection.CLIENTBOUND);
            CLIENTBOUND.addAll(clientPacketMap.multiconnect_values());
            IPacketHandler<?> serverPacketMap = packetHandlerMap.get(PacketDirection.SERVERBOUND);
            SERVERBOUND.addAll(serverPacketMap.multiconnect_values());
        }
    }

    private static class DefaultRegistry<T> {

        private static Map<Block, Item> DEFAULT_BLOCK_ITEMS = new HashMap<>();
        private static Map<EntityType<?>, SpawnEggItem> DEFAULT_SPAWN_EGG_ITEMS = new IdentityHashMap<>();
        private static IntIdentityHashBiMap<IDataSerializer<?>> DEFAULT_TRACKED_DATA_HANDLERS = new IntIdentityHashBiMap<>(16);

        private IntIdentityHashBiMap<T> defaultIndexedEntries = new IntIdentityHashBiMap<>(256);
        private BiMap<ResourceLocation, T> defaultEntries = HashBiMap.create();
        private int defaultNextId;

        private DefaultRegistry(Registry<T> registry) {
            for (T t : registry) {
                defaultIndexedEntries.put(t, registry.getId(t));
                defaultEntries.put(registry.getKey(t), t);
            }
            defaultNextId = ((ISimpleRegistry) registry).getNextFreeId();
        }

        public void restore(SimpleRegistry<T> registry) {
            @SuppressWarnings("unchecked") ISimpleRegistry<T> iregistry = (ISimpleRegistry<T>) registry;
            iregistry.getUnderlyingIntegerMap().clear();
            defaultIndexedEntries.iterator().forEachRemaining(t -> iregistry.getUnderlyingIntegerMap().put(t, defaultIndexedEntries.getId(t)));
            iregistry.getRegistryObjects().clear();
            iregistry.getRegistryObjects().putAll(defaultEntries);
            iregistry.setNextFreeId(defaultNextId);
        }

        public static Map<Registry<?>, DefaultRegistry<?>> DEFAULT_REGISTRIES = new LinkedHashMap<>();

        @SuppressWarnings("unchecked")
        public static <T> void restore(Registry<?> registry, DefaultRegistry<?> defaultRegistry) {
            ((DefaultRegistry<T>) defaultRegistry).restore((SimpleRegistry<T>) registry);
        }

        public static void restoreAll() {
            DEFAULT_REGISTRIES.forEach((DefaultRegistry::restore));
            Item.BLOCK_TO_ITEM.clear();
            Item.BLOCK_TO_ITEM.putAll(DEFAULT_BLOCK_ITEMS);
            SpawnEggItemAccessor.getSpawnEggs().clear();
            SpawnEggItemAccessor.getSpawnEggs().putAll(DEFAULT_SPAWN_EGG_ITEMS);
            DataSerializersAccessor.getHandlers().clear();
            for (IDataSerializer<?> handler : DEFAULT_TRACKED_DATA_HANDLERS)
                DataSerializersAccessor.getHandlers().put(handler, DEFAULT_TRACKED_DATA_HANDLERS.getId(handler));
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
            DEFAULT_REGISTRIES.put(Registry.MENU, new DefaultRegistry<>(Registry.MENU));
            DEFAULT_REGISTRIES.put(Registry.EFFECTS, new DefaultRegistry<>(Registry.EFFECTS));
            DEFAULT_REGISTRIES.put(Registry.RECIPE_SERIALIZER, new DefaultRegistry<>(Registry.RECIPE_SERIALIZER));
            DEFAULT_REGISTRIES.put(Registry.SOUND_EVENT, new DefaultRegistry<>(Registry.SOUND_EVENT));

            DEFAULT_BLOCK_ITEMS.putAll(Item.BLOCK_TO_ITEM);
            DEFAULT_SPAWN_EGG_ITEMS.putAll(SpawnEggItemAccessor.getSpawnEggs());
            for (IDataSerializer<?> handler : DataSerializersAccessor.getHandlers())
                DEFAULT_TRACKED_DATA_HANDLERS.put(handler, DataSerializersAccessor.getHandlers().getId(handler));

            //noinspection unchecked
            ((ISimpleRegistry<Block>) Registry.BLOCK).addRegisterListener(block -> {
                if (DEFAULT_BLOCK_ITEMS.containsKey(block)) {
                    Item item = DEFAULT_BLOCK_ITEMS.get(block);
                    Item.BLOCK_TO_ITEM.put(block, item);
                    //noinspection unchecked
                    reregister((ISimpleRegistry<Item>) Registry.ITEM, item);
                }
            });
            //noinspection unchecked
            ((ISimpleRegistry<Block>) Registry.BLOCK).addUnregisterListener(block -> {
                if (Item.BLOCK_TO_ITEM.containsKey(block)) {
                    //noinspection unchecked
                    ((ISimpleRegistry<Item>) Registry.ITEM).unregister(Item.BLOCK_TO_ITEM.remove(block));
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
                if (SpawnEggItemAccessor.getSpawnEggs().containsKey(entityType)) {
                    //noinspection unchecked
                    ((ISimpleRegistry<Item>) Registry.ITEM).unregister(SpawnEggItemAccessor.getSpawnEggs().remove(entityType));
                }
            });
        }
    }

}
