package net.earthcomputer.multiconnect.protocols.generic;

import com.google.common.base.Suppliers;
import net.earthcomputer.multiconnect.api.ThreadSafe;
import net.earthcomputer.multiconnect.impl.IUtils;
import net.earthcomputer.multiconnect.mixin.bridge.MinecraftClientAccessor;
import net.earthcomputer.multiconnect.protocols.generic.blockconnections.BlockConnections;
import net.earthcomputer.multiconnect.protocols.generic.blockconnections.BlockConnector;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
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
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.tag.RequiredTagListRegistry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.*;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public abstract class AbstractProtocol implements IUtils {
    private static final Logger LOGGER = LogManager.getLogger("multiconnect");

    private static final List<Block> collisionBoxesToRevert = new ArrayList<>();

    private int protocolVersion;
    private final Supplier<BlockConnector> lazyBlockConnector = Suppliers.memoize(() -> BlockConnections.buildConnector(protocolVersion));

    // To be called by ProtocolRegistry only!
    public void setProtocolVersion(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public void setup(boolean resourceReload) {
        revertCollisionBoxes();
        if (!resourceReload) {
            modifyPacketLists();
            DataTrackerManager.onConnectToServer();
        }
        doRegistryMutation(true);
        if (!resourceReload) {
            removeTrackedDataHandlers();
            AssetDownloader.reloadLanguages();
        }
        markChangedCollisionBoxes();
        ((MinecraftClientAccessor) MinecraftClient.getInstance()).callInitializeSearchableContainers();
        ((MinecraftClientAccessor) MinecraftClient.getInstance()).getSearchManager().reload(MinecraftClient.getInstance().getResourceManager());
    }

    public void disable() {
    }

    public void doRegistryMutation(boolean reAddMissingValues) {
        DefaultRegistries.restoreAll();
        for (DefaultDynamicRegistries<?> defaultDynamicRegistry : DefaultDynamicRegistries.getInstances()) {
            defaultDynamicRegistry.clear();
        }
        RegistryMutator mutator = new RegistryMutator();
        mutateRegistries(mutator);
        mutator.runMutations(DefaultRegistries.DEFAULT_REGISTRIES.keySet());
        DefaultRegistries.DEFAULT_REGISTRIES.keySet().forEach((registry -> postMutateRegistry(registry, reAddMissingValues)));
        recomputeBlockStates();
    }

    protected void modifyPacketLists() {
        //noinspection ConstantConditions
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
    private static <T extends PacketListener, P extends Packet<T>> void doRegister(IPacketHandler<T> handler, Class<?> packetClass, Function<PacketByteBuf, ?> factory) {
        handler.multiconnect_register((Class<P>) packetClass, (Function<PacketByteBuf, P>) factory);
    }

    @SuppressWarnings("unchecked")
    protected void recomputeBlockStates() {
        ((IIdList<BlockState>) Block.STATE_IDS).multiconnect_clear();
        for (Block block : Registry.BLOCK) {
            Stream<BlockState> states = getStatesForBlock(block).filter(this::acceptBlockState);
            Comparator<BlockState> order = getBlockStateOrder(block);
            if (order != null) {
                states = states.sorted(order);
            }
            states.forEach(Block.STATE_IDS::add);
        }
    }

    protected Stream<BlockState> getStatesForBlock(Block block) {
        return block.getStateManager().getStates().stream();
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

    @ThreadSafe
    public final boolean preSendPacket(Packet<?> packet) {
        if (packet instanceof IServerboundSlotPacket slotPacket) {
            if (!slotPacket.multiconnect_isProcessed()) {
                // Packets that go through the ClientPlayerInteractionManager have enough context to be translated, see MixinClientPlayerInteractionManager
                LOGGER.warn("Dropping untranslated serverbound click slot packet, sent without the client player interaction manager");
                return false;
            }
            if (slotPacket.multiconnect_getSlotId() == -1) {
                return false;
            }
        }

        return true;
    }

    @ThreadSafe
    public boolean onSendPacket(Packet<?> packet) {
        return true;
    }

    public void mutateRegistries(RegistryMutator mutator) {
    }

    @SuppressWarnings("unchecked")
    private <T> void postMutateRegistry(Registry<T> registry, boolean reAddMissingValues) {
        if (!(registry instanceof SimpleRegistry)) return;
        ISimpleRegistry<T> iregistry = (ISimpleRegistry<T>) registry;
        iregistry.lockRealEntries();
        if (!reAddMissingValues) {
            return;
        }
        DefaultRegistries<T> defaultRegistries = (DefaultRegistries<T>) DefaultRegistries.DEFAULT_REGISTRIES.get(registry);
        if (defaultRegistries == null) return;

        Identifier defaultId;
        T defaultValue;
        if (registry instanceof DefaultedRegistry<T> defaultedRegistry) {
            defaultId = defaultedRegistry.getDefaultId();
            defaultValue = defaultedRegistry.get(defaultId);
        } else {
            defaultId = null;
            defaultValue = null;
        }

        for (Map.Entry<Identifier, T> entry : defaultRegistries.defaultIdToEntry.entrySet()) {
            if (Objects.equals(registry.getId(entry.getValue()), defaultId) && entry.getValue() != defaultValue) {
                Identifier id = entry.getKey();
                for (int suffix = 1; registry.containsId(id); suffix++) {
                    id = new Identifier(entry.getKey().getNamespace(), entry.getKey().getPath() + suffix);
                }
                RegistryKey<T> key = RegistryKey.of(iregistry.getRegistryKey(), id);
                iregistry.register(entry.getValue(), iregistry.getNextId(), key, false);
            }
        }
    }

    @ThreadSafe(withGameThread = false)
    public void mutateDynamicRegistries(DynamicRegistryManager.Impl registries) {
    }

    public boolean acceptBlockState(BlockState state) {
        return true;
    }

    public void preAcceptEntityData(Class<? extends Entity> clazz, TrackedData<?> data) {
    }

    public boolean acceptEntityData(Class<? extends Entity> clazz, TrackedData<?> data) {
        return true;
    }

    protected void markChangedCollisionBoxes() {
    }

    private void revertCollisionBoxes() {
        if (!collisionBoxesToRevert.isEmpty()) {
            // Lithium compat: make sure tags have been initialized before initializing shape cache
            RequiredTagListRegistry.clearAllTags();
        }
        for (Block block : collisionBoxesToRevert) {
            for (BlockState state : block.getStateManager().getStates()) {
                state.initShapeCache();
            }
        }
        collisionBoxesToRevert.clear();
    }

    protected void markCollisionBoxChanged(Block block) {
        if (collisionBoxesToRevert.isEmpty()) {
            // Lithium compat: make sure tags have been initialized before initializing shape cache
            RequiredTagListRegistry.clearAllTags();
        }
        for (BlockState state : block.getStateManager().getStates()) {
            state.initShapeCache();
        }
        collisionBoxesToRevert.add(block);
    }

    public BlockState getActualState(World world, BlockPos pos, BlockState state) {
        return state;
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

    public void addExtraGameEventTags(TagRegistry<GameEvent> tags) {
    }

    @ThreadSafe
    public BlockConnector getBlockConnector() {
        return lazyBlockConnector.get();
    }

    public boolean shouldBlockChangeReplaceBlockEntity(Block oldBlock, Block newBlock) {
        return oldBlock != newBlock;
    }

    public float getBlockHardness(BlockState state, float hardness) {
        return hardness;
    }

    public float getBlockResistance(Block block, float resistance) {
        return resistance;
    }

    public int clientSlotIdToServer(ScreenHandler screenHandler, int slotId) {
        return slotId;
    }

    public int serverSlotIdToClient(ScreenHandler screenHandler, int slotId) {
        return slotId;
    }

    /**
     * Individual pieces of translation (e.g. heightmap translation for 1.13.2 <-> 1.14) should be directly translated
     * to the current version.
     */
    @ThreadSafe(withGameThread = false)
    public void postTranslateChunk(ChunkDataTranslator translator, ChunkData data) {
    }

    /**
     * Whether the given packet should translate async (and not block the network thread). Currently only packets with
     * an associated chunk position can be translated. Override {@link #extractChunkPos} as well to extract the chunk
     * pos of a packet.
     */
    @ThreadSafe(withGameThread = false)
    public boolean shouldTranslateAsync(Class<? extends Packet<?>> packetClass) {
        return packetClass == ChunkDataS2CPacket.class;
    }

    @ThreadSafe
    public ChunkPos extractChunkPos(Class<? extends Packet<?>> packetClass, PacketByteBuf buf) {
        if (packetClass == ChunkDataS2CPacket.class) {
            return new ChunkPos(buf.readInt(), buf.readInt());
        } else {
            throw new IllegalArgumentException();
        }
    }

    static {
        DefaultPackets.initialize();
    }

    private static class DefaultPackets {
        private static final List<PacketInfo<?>> CLIENTBOUND = new ArrayList<>();
        private static final List<PacketInfo<?>> SERVERBOUND = new ArrayList<>();

        private static void initialize() {
            //noinspection ConstantConditions
            var packetHandlerMap = ((INetworkState) (Object) NetworkState.PLAY).getPacketHandlers();
            IPacketHandler<?> clientPacketMap = packetHandlerMap.get(NetworkSide.CLIENTBOUND);
            CLIENTBOUND.addAll(clientPacketMap.multiconnect_values());
            IPacketHandler<?> serverPacketMap = packetHandlerMap.get(NetworkSide.SERVERBOUND);
            SERVERBOUND.addAll(serverPacketMap.multiconnect_values());
        }
    }

}
