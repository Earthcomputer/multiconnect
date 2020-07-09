package net.earthcomputer.multiconnect.protocols.generic;

import net.earthcomputer.multiconnect.impl.IUtils;
import net.earthcomputer.multiconnect.mixin.bridge.MinecraftClientAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.class_5455;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.NetworkState;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

public abstract class AbstractProtocol implements IUtils {

    public void setup(boolean resourceReload) {
        if (!resourceReload) {
            modifyPacketLists();
            DataTrackerManager.onConnectToServer();
        }
        doRegistryMutation(true);
        if (!resourceReload) {
            removeTrackedDataHandlers();
            OldLanguageManager.reloadLanguages();
        }
        ((MinecraftClientAccessor) MinecraftClient.getInstance()).callInitializeSearchableContainers();
        ((MinecraftClientAccessor) MinecraftClient.getInstance()).getSearchManager().apply(MinecraftClient.getInstance().getResourceManager());
    }

    public void doRegistryMutation(boolean reAddMissingValues) {
        DefaultRegistries.restoreAll();
        RegistryMutator mutator = new RegistryMutator();
        mutateRegistries(mutator);
        mutator.runMutations(DefaultRegistries.DEFAULT_REGISTRIES.keySet());
        DefaultRegistries.DEFAULT_REGISTRIES.keySet().forEach((registry -> postMutateRegistry(registry, reAddMissingValues)));
        recomputeBlockStates();
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

    protected void recomputeBlockStates() {
        ((IIdList) Block.STATE_IDS).multiconnect_clear();
        for (Block block : Registry.BLOCK) {
            Stream<BlockState> states = block.getStateManager().getStates().stream().filter(this::acceptBlockState);
            Comparator<BlockState> order = getBlockStateOrder(block);
            if (order != null) {
                states = states.sorted(order);
            }
            states.forEach(Block.STATE_IDS::add);
        }
    }

    protected Comparator<BlockState> getBlockStateOrder(Block block) {
        return null;
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
    private <T> void postMutateRegistry(Registry<T> registry, boolean reAddMissingValues) {
        if (!reAddMissingValues) {
            return;
        }
        if (!(registry instanceof SimpleRegistry)) return;
        if (registry instanceof DefaultedRegistry) return;
        ISimpleRegistry<T> iregistry = (ISimpleRegistry<T>) registry;
        DefaultRegistries<T> defaultRegistries = (DefaultRegistries<T>) DefaultRegistries.DEFAULT_REGISTRIES.get(registry);
        if (defaultRegistries == null) return;
        for (Map.Entry<Identifier, T> entry : defaultRegistries.defaultEntriesById.entrySet()) {
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

    public void addExtraBlockTags(TagRegistry<Block> tags) {
    }

    public void addExtraItemTags(TagRegistry<Item> tags, TagRegistry<Block> blockTags) {
    }

    public void addExtraFluidTags(TagRegistry<Fluid> tags) {
    }

    public void addExtraEntityTags(TagRegistry<EntityType<?>> tags) {
    }

    public boolean shouldBlockChangeReplaceBlockEntity(Block oldBlock, Block newBlock) {
        return oldBlock != newBlock;
    }

    static {
        DefaultPackets.initialize();
        DefaultRegistries.initialize();
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

}
