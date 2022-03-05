package net.earthcomputer.multiconnect.mixin.bridge;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.impl.Constants;
import net.earthcomputer.multiconnect.impl.Utils;
import net.earthcomputer.multiconnect.protocols.generic.*;
import net.earthcomputer.multiconnect.protocols.generic.blockconnections.BlockConnections;
import net.earthcomputer.multiconnect.protocols.generic.blockconnections.ChunkConnector;
import net.earthcomputer.multiconnect.protocols.generic.blockconnections.IBlockConnectableChunk;
import net.earthcomputer.multiconnect.protocols.v1_14_4.IBiomeStorage_1_14_4;
import net.earthcomputer.multiconnect.protocols.v1_14_4.Protocol_1_14_4;
import net.earthcomputer.multiconnect.protocols.v1_16_5.mixin.DimensionTypeAccessor;
import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockEventS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.LightUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.SynchronizeTagsS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.tag.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.dimension.DimensionType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Mixin(value = ClientPlayNetworkHandler.class, priority = -1000)
public class MixinClientPlayNetworkHandler implements IClientPlayNetworkHandler {
    @Unique private static final Logger MULTICONNECT_LOGGER = LogManager.getLogger("multiconnect");
    @Unique private static final boolean MULTICONNECT_CHECK_REQUIRED_TAGS = Boolean.getBoolean("multiconnect.checkRequiredTags");

    @Shadow private ClientWorld world;
    @Shadow @Final private MinecraftClient client;
    @Shadow private DynamicRegistryManager.Immutable registryManager;

    @Unique private ChunkDataS2CPacket currentChunkPacket;

    @Unique private final Cache<ChunkPos, List<Packet<ClientPlayPacketListener>>> afterChunkLoadPackets = CacheBuilder.newBuilder()
            .expireAfterWrite(Constants.PACKET_QUEUE_DROP_TIMEOUT, TimeUnit.SECONDS)
            .removalListener((RemovalListener<ChunkPos, List<Packet<ClientPlayPacketListener>>>) notification -> {
                if (notification.wasEvicted()) {
                    MULTICONNECT_LOGGER.warn("{} packets for chunk {}, {} were dropped due to the chunk not being loaded", notification.getValue().size(), notification.getKey().x, notification.getKey().z);
                }
            })
            .build();
    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstruct(CallbackInfo ci) {
        Utils.autoCleanUp(afterChunkLoadPackets, Constants.PACKET_QUEUE_DROP_TIMEOUT, TimeUnit.SECONDS);
    }

    @Inject(method = "onChunkData", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift = At.Shift.AFTER), cancellable = true)
    private void onOnChunkData(ChunkDataS2CPacket packet, CallbackInfo ci) {
        boolean canceled = false;
        if (ConnectionInfo.protocolVersion != SharedConstants.getGameVersion().getProtocolVersion()) {
            if (!((IUserDataHolder) packet).multiconnect_getUserData(ChunkDataTranslator.DATA_TRANSLATED_KEY)) {
                ChunkDataTranslator.submit(packet);
                ci.cancel();
                canceled = true;
            } else if (((IUserDataHolder) packet).multiconnect_getUserData(ChunkDataTranslator.DIMENSION_KEY) != world.getDimension()) {
                ci.cancel();
                canceled = true;
            }
        }
        if (!canceled) {
            currentChunkPacket = packet;
        }
    }

    @Override
    public void multiconnect_onAfterChunkData(ChunkDataS2CPacket packet) {
        currentChunkPacket = null;
        ChunkPos pos = new ChunkPos(packet.getX(), packet.getZ());
        List<Packet<ClientPlayPacketListener>> packets = afterChunkLoadPackets.asMap().remove(pos);
        if (packets != null) {
            for (Packet<ClientPlayPacketListener> afterChunkLoadPacket : packets) {
                afterChunkLoadPacket.apply((ClientPlayPacketListener) this);
            }
        }
    }

    @Redirect(method = "loadChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientChunkManager;loadChunkFromPacket(IILnet/minecraft/network/PacketByteBuf;Lnet/minecraft/nbt/NbtCompound;Ljava/util/function/Consumer;)Lnet/minecraft/world/chunk/WorldChunk;"))
    private WorldChunk fixChunk(
            ClientChunkManager instance,
            int x,
            int z,
            PacketByteBuf buf,
            NbtCompound nbt,
            Consumer<net.minecraft.network.packet.s2c.play.ChunkData.BlockEntityVisitor> blockEntityVisitor
    ) {
        WorldChunk chunk = instance.loadChunkFromPacket(x, z, buf, nbt, blockEntityVisitor);
        if (ConnectionInfo.protocolVersion != SharedConstants.getGameVersion().getProtocolVersion()) {
            if (chunk != null && !Utils.isChunkEmpty(chunk)) {
                var blocksNeedingUpdate = ((IUserDataHolder) currentChunkPacket).multiconnect_getUserData(BlockConnections.BLOCKS_NEEDING_UPDATE_KEY);
                ChunkConnector chunkConnector = new ChunkConnector(chunk, ConnectionInfo.protocol.getBlockConnector(), blocksNeedingUpdate);
                ((IBlockConnectableChunk) chunk).multiconnect_setChunkConnector(chunkConnector);
                for (Direction side : Direction.Type.HORIZONTAL) {
                    Chunk neighborChunk = world.getChunk(x + side.getOffsetX(),
                            z + side.getOffsetZ(), ChunkStatus.FULL, false);
                    if (neighborChunk != null) {
                        chunkConnector.onNeighborChunkLoaded(side);
                        ChunkConnector neighborConnector = ((IBlockConnectableChunk) neighborChunk).multiconnect_getChunkConnector();
                        if (neighborConnector != null) {
                            neighborConnector.onNeighborChunkLoaded(side.getOpposite());
                        }
                    }
                }
            }
        }

        if (ConnectionInfo.protocolVersion <= Protocols.V1_14_4) {
            if (chunk != null) {
                Biome[] biomeData = ((IUserDataHolder) currentChunkPacket).multiconnect_getUserData(Protocol_1_14_4.BIOME_DATA_KEY);
                if (biomeData != null) {
                    ((IBiomeStorage_1_14_4) chunk).multiconnect_setBiomeArray_1_14_4(biomeData);
                }
            }
        }
        return chunk;
    }

    @Inject(method = "method_34007", remap = false, at = @At("RETURN"))
    private void fixDeltaChunk(int flags, BlockPos pos, BlockState state, CallbackInfo ci) {
        Chunk chunk = world.getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.FULL, false);
        if (chunk != null) {
            ChunkConnector connector = ((IBlockConnectableChunk) chunk).multiconnect_getChunkConnector();
            if (connector != null) {
                connector.onBlockChange(pos, state.getBlock(), true);
            }

            BlockState currentState = world.getBlockState(pos);
            BlockState newState = ConnectionInfo.protocol.getActualState(world, pos, currentState);
            if (newState != currentState) {
                world.setBlockState(pos, newState, Block.NOTIFY_ALL | Block.FORCE_STATE | Block.SKIP_LIGHTING_UPDATES);
            }
        }
    }

    @Inject(method = "onChunkDeltaUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift = At.Shift.AFTER), cancellable = true)
    private void onOnChunkDeltaUpdate(ChunkDeltaUpdateS2CPacket packet, CallbackInfo ci) {
        ChunkSectionPos sectionPos = ((ChunkDeltaUpdateS2CAccessor) packet).getSectionPos();
        waitForLoadedChunk(packet, new ChunkPos(sectionPos.getX(), sectionPos.getZ()), ci);
    }

    @Inject(method = "onBlockUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift = At.Shift.AFTER), cancellable = true)
    private void onOnBlockUpdate(BlockUpdateS2CPacket packet, CallbackInfo ci) {
        waitForLoadedChunk(packet, new ChunkPos(packet.getPos()), ci);
    }

    @Inject(method = "onBlockEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift = At.Shift.AFTER), cancellable = true)
    private void onOnBlockEvent(BlockEventS2CPacket packet, CallbackInfo ci) {
        waitForLoadedChunk(packet, new ChunkPos(packet.getPos()), ci);
    }

    @Inject(method = "onBlockEntityUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift = At.Shift.AFTER), cancellable = true)
    private void onOnBlockEntityUpdate(BlockEntityUpdateS2CPacket packet, CallbackInfo ci) {
        waitForLoadedChunk(packet, new ChunkPos(packet.getPos()), ci);
    }

    @Inject(method = "onLightUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift = At.Shift.AFTER), cancellable = true)
    private void onOnLightUpdate(LightUpdateS2CPacket packet, CallbackInfo ci) {
        waitForLoadedChunk(packet, new ChunkPos(packet.getChunkX(), packet.getChunkZ()), ci);
    }

    @Unique
    private void waitForLoadedChunk(Packet<ClientPlayPacketListener> packet, ChunkPos pos, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion != SharedConstants.getProtocolVersion()) {
            if (world.getChunk(pos.x, pos.z, ChunkStatus.FULL, false) == null) {
                afterChunkLoadPackets.asMap().computeIfAbsent(pos, k -> new ArrayList<>()).add(packet);
                ci.cancel();
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Unique
    private static <T> void copyValues(Registry<T> from, SimpleRegistry<?> _to) {
        SimpleRegistry<T> to = (SimpleRegistry<T>) _to;
        RegistryBuilder<T> builder = new RegistryBuilder<>(to);
        builder.clear();
        for (T value : from) {
            builder.registerInPlace(from.getRawId(value), value, from.getId(value));
        }
        if (to.getKey() == (RegistryKey<?>) Registry.DIMENSION_TYPE_KEY) {
            for (T value : to) {
                if (!builder.contains(value)) {
                    builder.register(builder.getNextId(), value, to.getId(value));
                }
            }
        }
        builder.apply();
    }

    @Inject(method = "onGameJoin", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;registryManager:Lnet/minecraft/util/registry/DynamicRegistryManager$Immutable;", shift = At.Shift.AFTER, ordinal = 0))
    private void onOnGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
        var registries = packet.registryManager();
        assert registries != null;
        var mutableRegistries = DynamicRegistryManager.createAndLoad();
        registries.streamManagedRegistries().forEach(registry -> {
            copyValues(registry.value(), (SimpleRegistry<?>) mutableRegistries.getMutable(registry.key()));
        });

        mutableRegistries.streamManagedRegistries().forEach(registry -> {
            if (registry.key() != Registry.DIMENSION_TYPE_KEY) {
                addMissingValues(getBuiltinRegistry(registry.key()), registry.value());
            }
        });

        // fix world height
        DimensionTypeAccessor overworld = (DimensionTypeAccessor) mutableRegistries.getMutable(Registry.DIMENSION_TYPE_KEY).get(DimensionType.OVERWORLD_REGISTRY_KEY);
        if (ConnectionInfo.protocolVersion <= Protocols.V1_17_1) {
            assert overworld != null;
            overworld.setMinimumY(0);
            overworld.setHeight(256);
            overworld.setLogicalHeight(256);
        }

        this.registryManager = mutableRegistries.toImmutable();
    }

    @SuppressWarnings("unchecked")
    @Unique
    private static <T, R extends Registry<T>> Registry<?> getBuiltinRegistry(RegistryKey<? extends Registry<?>> registryKey) {
        return ((Registry<R>) BuiltinRegistries.REGISTRIES).getOrEmpty((RegistryKey<R>) registryKey)
                .orElseGet(() -> ((Registry<R>) Registry.REGISTRIES).get((RegistryKey<R>) registryKey));
    }

    @SuppressWarnings("unchecked")
    @Unique
    private static <T> void addMissingValues(Registry<T> builtinRegistry, Registry<?> _dynamicRegistry) {
        Registry<T> dynamicRegistry = (Registry<T>) _dynamicRegistry;
        ISimpleRegistry<T> iregistry = (ISimpleRegistry<T>) dynamicRegistry;
        iregistry.multiconnect_lockRealEntries();
        for (T val : builtinRegistry) {
            builtinRegistry.getKey(val).ifPresent(key -> {
                if (dynamicRegistry.getOrEmpty(key).isEmpty()) {
                    Registry.register(dynamicRegistry, key, val);
                }
            });
        }
    }

    @Inject(method = "onSynchronizeTags", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift = At.Shift.AFTER))
    private void onOnSynchronizeTags(SynchronizeTagsS2CPacket packet, CallbackInfo ci) {
        TagRegistry<Block> blockTagRegistry = new TagRegistry<>(Registry.BLOCK);
        var blockTags = setExtraTags("block", packet, blockTagRegistry,
                BlockTags.class, ConnectionInfo.protocol::addExtraBlockTags);
        var itemTags = setExtraTags("item", packet, new TagRegistry<>(Registry.ITEM),
                ItemTags.class,
                itemTagRegistry -> ConnectionInfo.protocol.addExtraItemTags(itemTagRegistry, blockTagRegistry));
        var fluidTags = setExtraTags("fluid", packet, new TagRegistry<>(Registry.FLUID),
                FluidTags.class, ConnectionInfo.protocol::addExtraFluidTags);
        var entityTypeTags = setExtraTags("entity type", packet,
                new TagRegistry<>(Registry.ENTITY_TYPE), EntityTypeTags.class,
                ConnectionInfo.protocol::addExtraEntityTags);
        var gameEventTags = setExtraTags("game event", packet, new TagRegistry<>(Registry.GAME_EVENT),
                GameEventTags.class, ConnectionInfo.protocol::addExtraGameEventTags);
        packet.getGroups().put(Registry.BLOCK_KEY, blockTags);
        packet.getGroups().put(Registry.ITEM_KEY, itemTags);
        packet.getGroups().put(Registry.FLUID_KEY, fluidTags);
        packet.getGroups().put(Registry.ENTITY_TYPE_KEY, entityTypeTags);
        packet.getGroups().put(Registry.GAME_EVENT_KEY, gameEventTags);
    }

    @Unique
    private static <T> TagPacketSerializer.Serialized setExtraTags(String type, SynchronizeTagsS2CPacket packet, TagRegistry<T> tagRegistry, Class<?> requiredTagsClass, Consumer<TagRegistry<T>> tagsAdder) {
        Registry<T> registry = tagRegistry.getRegistry();
        TagPacketSerializer.Serialized existingTags = packet.getGroups().get(registry.getKey());
        if (existingTags != null) {
            TagPacketSerializer.loadTags(
                    registry.getKey(),
                    registry,
                    existingTags,
                    (k, v) -> tagRegistry.put(k, v.stream().map(RegistryEntry::value).collect(Collectors.toCollection(HashSet::new)))
            );
        }
        tagsAdder.accept(tagRegistry);
        if (MULTICONNECT_CHECK_REQUIRED_TAGS) {
            checkMissingTags(type, tagRegistry.keySet(), requiredTagsClass);
        }

        Map<Identifier, IntList> serialized = new HashMap<>(tagRegistry.size());
        tagRegistry.forEach((key, values) -> {
            IntList ids = new IntArrayList(values.size());
            for (T value : values) {
                ids.add(registry.getRawId(value));
            }
            serialized.put(key.id(), ids);
        });
        return TagPacketSerializerSerializedAccessor.createSerialized(serialized);
    }

    @SuppressWarnings("unchecked")
    @Unique
    private static <T> void checkMissingTags(String type, Set<TagKey<T>> existingTags, Class<?> requiredTagsClass) {
        final int publicStaticFinal = Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL;
        List<TagKey<T>> missingTags = new ArrayList<>(0);
        for (Field field : requiredTagsClass.getFields()) {
            if ((field.getModifiers() & publicStaticFinal) == publicStaticFinal && field.getType() == TagKey.class) {
                TagKey<T> tagKey;
                try {
                    tagKey = (TagKey<T>) field.get(null);
                } catch (ReflectiveOperationException e) {
                    MULTICONNECT_LOGGER.error("Error occurred getting tag key", e);
                    continue;
                }
                if (tagKey != null && !existingTags.contains(tagKey)) {
                    missingTags.add(tagKey);
                }
            }
        }

        if (!missingTags.isEmpty()) {
            MULTICONNECT_LOGGER.error("Found missing {} tags: {}", type, missingTags);
        }
    }

    @Inject(method = "onCustomPayload", at = @At("HEAD"), cancellable = true)
    private void onOnCustomPayload(CustomPayloadS2CPacket packet, CallbackInfo ci) {
        if (packet.getChannel().equals(CustomPayloadHandler.DROP_ID)) {
            ci.cancel();
        } else if (ConnectionInfo.protocolVersion != SharedConstants.getGameVersion().getProtocolVersion()
                && !CustomPayloadHandler.VANILLA_CLIENTBOUND_CHANNELS.contains(packet.getChannel())) {
            NetworkThreadUtils.forceMainThread(packet, (ClientPlayNetworkHandler) (Object) this, MinecraftClient.getInstance());
            CustomPayloadHandler.handleClientboundCustomPayload((ClientPlayNetworkHandler) (Object) this, packet);
            ci.cancel();
        }
    }

    @ModifyVariable(method = "onScreenHandlerSlotUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift = At.Shift.AFTER), ordinal = 0, argsOnly = true)
    private ScreenHandlerSlotUpdateS2CPacket modifySlotUpdatePacket(ScreenHandlerSlotUpdateS2CPacket packet) {
        ClientPlayerEntity player = client.player;
        assert player != null;
        ScreenHandler screenHandler = player.currentScreenHandler;
        if (packet.getSyncId() != screenHandler.syncId) {
            return packet;
        }

        int slot = ConnectionInfo.protocol.serverSlotIdToClient(screenHandler, packet.getSlot());
        if (slot != packet.getSlot()) {
            packet = new ScreenHandlerSlotUpdateS2CPacket(packet.getSyncId(), packet.getRevision(), slot, packet.getItemStack());
        }

        return packet;
    }

    @ModifyVariable(method = "onInventory", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift = At.Shift.AFTER), ordinal = 0, argsOnly = true)
    private InventoryS2CPacket modifyInventoryPacket(InventoryS2CPacket packet) {
        ClientPlayerEntity player = client.player;
        assert player != null;
        ScreenHandler screenHandler = player.currentScreenHandler;
        if (packet.getSyncId() != screenHandler.syncId) {
            return packet;
        }

        List<ItemStack> newStacks = new ArrayList<>(packet.getContents().size());
        boolean modified = false;
        for (int oldSlotId = 0; oldSlotId < packet.getContents().size(); oldSlotId++) {
            int newSlotId = ConnectionInfo.protocol.serverSlotIdToClient(screenHandler, oldSlotId);
            while (newStacks.size() <= newSlotId) {
                newStacks.add(ItemStack.EMPTY);
            }
            newStacks.set(newSlotId, packet.getContents().get(oldSlotId));
            modified |= newSlotId != oldSlotId;
        }

        if (modified) {
            DefaultedList<ItemStack> newContents = DefaultedList.copyOf(ItemStack.EMPTY, newStacks.toArray(ItemStack[]::new));
            packet = new InventoryS2CPacket(packet.getSyncId(), packet.getRevision(), newContents, packet.getCursorStack());
        }

        return packet;
    }

}
