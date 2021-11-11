package net.earthcomputer.multiconnect.mixin.bridge;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
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
import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.class_6603;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
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
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.event.GameEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Mixin(value = ClientPlayNetworkHandler.class, priority = -1000)
public class MixinClientPlayNetworkHandler {
    @Unique private static final Logger MULTICONNECT_LOGGER = LogManager.getLogger("multiconnect");

    @Shadow private ClientWorld world;
    @Shadow @Final private MinecraftClient client;
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

    @Inject(method = "onChunkData", at = @At("TAIL"))
    private void afterOnChunkData(ChunkDataS2CPacket packet, CallbackInfo ci) {
        currentChunkPacket = null;
        ChunkPos pos = new ChunkPos(packet.getX(), packet.getZ());
        List<Packet<ClientPlayPacketListener>> packets = afterChunkLoadPackets.asMap().remove(pos);
        if (packets != null) {
            for (Packet<ClientPlayPacketListener> afterChunkLoadPacket : packets) {
                afterChunkLoadPacket.apply((ClientPlayPacketListener) this);
            }
        }
    }

    @Redirect(method = "method_38539", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientChunkManager;loadChunkFromPacket(IILnet/minecraft/network/PacketByteBuf;Lnet/minecraft/nbt/NbtCompound;Ljava/util/function/Consumer;)Lnet/minecraft/world/chunk/WorldChunk;"))
    private WorldChunk fixChunk(ClientChunkManager instance, int x, int z, PacketByteBuf buf, NbtCompound nbt, Consumer<class_6603.class_6605> blockEntityProcessor) {
        WorldChunk chunk = instance.loadChunkFromPacket(x, z, buf, nbt, blockEntityProcessor);
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

    @Inject(method = "onGameJoin", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift = At.Shift.AFTER))
    private void onOnGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
        var registries = (DynamicRegistryManager.Impl) packet.registryManager();
        assert registries != null;
        //noinspection ConstantConditions
        var registriesAccessor = (DynamicRegistryManagerImplAccessor) (Object) registries;
        registriesAccessor.setRegistries(new HashMap<>(registriesAccessor.getRegistries())); // make registries mutable

        for (var registryKey : DynamicRegistryManagerAccessor.getInfos().keySet()) {
            if (registryKey != Registry.DIMENSION_TYPE_KEY && DynamicRegistryManagerAccessor.getInfos().get(registryKey).isSynced()) {
                addMissingValues(getBuiltinRegistry(registryKey), registries);
            }
        }

        registriesAccessor.setRegistries(ImmutableMap.copyOf(registriesAccessor.getRegistries())); // make immutable again (faster)
    }

    @SuppressWarnings("unchecked")
    @Unique
    private static <T, R extends Registry<T>> Registry<?> getBuiltinRegistry(RegistryKey<? extends Registry<?>> registryKey) {
        return ((Registry<R>) BuiltinRegistries.REGISTRIES).get((RegistryKey<R>) registryKey);
    }

    @SuppressWarnings("unchecked")
    @Unique
    private static <T> void addMissingValues(Registry<T> builtinRegistry, DynamicRegistryManager.Impl registries) {
        Registry<T> dynamicRegistry =  registries.get(builtinRegistry.getKey());
        ISimpleRegistry<T> iregistry = (ISimpleRegistry<T>) dynamicRegistry;
        iregistry.lockRealEntries();
        for (T val : builtinRegistry) {
            builtinRegistry.getKey(val).ifPresent(key -> {
                if (dynamicRegistry.getOrEmpty(key).isEmpty()) {
                    iregistry.register(val, iregistry.getNextId(), key, false);
                }
            });
        }
    }

    @Inject(method = "onSynchronizeTags", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift = At.Shift.AFTER))
    private void onOnSynchronizeTags(SynchronizeTagsS2CPacket packet, CallbackInfo ci) {
        var requiredTags = new HashMap<RegistryKey<? extends Registry<?>>, List<Identifier>>();
        RequiredTagListRegistry.forEach(requiredTagList -> {
            var tagWrappers = ((RequiredTagListAccessor<?>) requiredTagList).getTags();
            List<Identifier> tagIds =
                    tagWrappers.stream().map(RequiredTagList.TagWrapper::getId).collect(Collectors.toList());
            requiredTags.put(requiredTagList.getRegistryKey(), tagIds);
        });
        TagRegistry<Block> blockTagRegistry = new TagRegistry<>(Registry.BLOCK);
        TagGroup<Block> blockTags = setExtraTags("block", packet, blockTagRegistry,
                requiredTags.get(Registry.BLOCK_KEY), ConnectionInfo.protocol::addExtraBlockTags);
        TagGroup<Item> itemTags = setExtraTags("item", packet, new TagRegistry<>(Registry.ITEM),
                requiredTags.get(Registry.ITEM_KEY),
                itemTagRegistry -> ConnectionInfo.protocol.addExtraItemTags(itemTagRegistry, blockTagRegistry));
        TagGroup<Fluid> fluidTags = setExtraTags("fluid", packet, new TagRegistry<>(Registry.FLUID),
                requiredTags.get(Registry.FLUID_KEY), ConnectionInfo.protocol::addExtraFluidTags);
        TagGroup<EntityType<?>> entityTypeTags = setExtraTags("entity type", packet,
                new TagRegistry<>(Registry.ENTITY_TYPE), requiredTags.get(Registry.ENTITY_TYPE_KEY),
                ConnectionInfo.protocol::addExtraEntityTags);
        TagGroup<GameEvent> gameEventTags = setExtraTags("game event", packet, new TagRegistry<>(Registry.GAME_EVENT),
                requiredTags.get(Registry.GAME_EVENT_KEY), ConnectionInfo.protocol::addExtraGameEventTags);
        packet.getGroups().put(Registry.BLOCK_KEY, blockTags.serialize(Registry.BLOCK));
        packet.getGroups().put(Registry.ITEM_KEY, itemTags.serialize(Registry.ITEM));
        packet.getGroups().put(Registry.FLUID_KEY, fluidTags.serialize(Registry.FLUID));
        packet.getGroups().put(Registry.ENTITY_TYPE_KEY, entityTypeTags.serialize(Registry.ENTITY_TYPE));
        packet.getGroups().put(Registry.GAME_EVENT_KEY, gameEventTags.serialize(Registry.GAME_EVENT));
    }

    @Unique
    private static <T> TagGroup<T> setExtraTags(String type, SynchronizeTagsS2CPacket packet, TagRegistry<T> tagRegistry, List<Identifier> requiredTags, Consumer<TagRegistry<T>> tagsAdder) {
        Registry<T> registry = tagRegistry.getRegistry();
        if (packet.getGroups().containsKey(registry.getKey())) {
            TagGroup<T> group = TagGroup.deserialize(packet.getGroups().get(registry.getKey()), registry);
            group.getTags().forEach((id, tag) -> tagRegistry.put(id, new HashSet<>(tag.values())));
        }
        tagsAdder.accept(tagRegistry);
        BiMap<Identifier, Tag<T>> tagBiMap = HashBiMap.create(tagRegistry.size());
        tagRegistry.forEach((id, set) -> tagBiMap.put(id, Tag.of(set)));

        // ViaVersion doesn't send all required tags to older clients because they didn't check them. We have to add empty ones to substitute.
        if (ConnectionInfo.protocolVersion <= Protocols.V1_16_1) {
            List<Identifier> missingTagIds = new ArrayList<>(requiredTags);
            missingTagIds.removeAll(tagBiMap.keySet());
            if (!missingTagIds.isEmpty()) {
                MULTICONNECT_LOGGER.warn("Server didn't send required {} tags, adding empty substitutes for {}", type, missingTagIds);
                for (Identifier missingTagId : missingTagIds) {
                    tagBiMap.put(missingTagId, SetTag.empty());
                }
            }
        }

        return TagGroup.create(tagBiMap);
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
