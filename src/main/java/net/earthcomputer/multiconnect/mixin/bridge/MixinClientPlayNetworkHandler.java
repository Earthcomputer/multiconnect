package net.earthcomputer.multiconnect.mixin.bridge;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.generic.*;
import net.earthcomputer.multiconnect.protocols.generic.blockconnections.ChunkConnector;
import net.earthcomputer.multiconnect.protocols.generic.blockconnections.IBlockConnectableChunk;
import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.SynchronizeTagsS2CPacket;
import net.minecraft.tag.*;
import net.minecraft.util.EightWayDirection;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;
import java.util.function.Consumer;

@Mixin(value = ClientPlayNetworkHandler.class, priority = -1000)
public class MixinClientPlayNetworkHandler {

    @Shadow private ClientWorld world;

    @Unique private static final Logger MULTICONNECT_LOGGER = LogManager.getLogger("multiconnect");

    @Inject(method = "onChunkData", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift = At.Shift.AFTER), cancellable = true)
    private void onOnChunkData(ChunkDataS2CPacket packet, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion != SharedConstants.getGameVersion().getProtocolVersion()) {
            if (!((IChunkDataS2CPacket) packet).multiconnect_isDataTranslated()) {
                ChunkDataTranslator.submit(packet);
                ci.cancel();
            } else if (((IChunkDataS2CPacket) packet).multiconnect_getDimension() != world.getDimension()) {
                ci.cancel();
            }
        }
    }

    @ModifyVariable(method = "onChunkData", ordinal = 0, at = @At(value = "STORE", ordinal = 0))
    private WorldChunk fixChunk(WorldChunk chunk, ChunkDataS2CPacket packet) {
        if (ConnectionInfo.protocolVersion != SharedConstants.getGameVersion().getProtocolVersion()) {
            if (chunk != null) {
                EnumMap<EightWayDirection, ShortSet> blocksNeedingUpdate = ((IChunkDataS2CPacket) packet).multiconnect_getBlocksNeedingUpdate();
                ChunkConnector chunkConnector = new ChunkConnector(chunk, ConnectionInfo.protocol.getBlockConnector(), blocksNeedingUpdate);
                ((IBlockConnectableChunk) chunk).multiconnect_setChunkConnector(chunkConnector);
                for (Direction side : Direction.Type.HORIZONTAL) {
                    Chunk neighborChunk = world.getChunk(packet.getX() + side.getOffsetX(), packet.getZ() + side.getOffsetZ(), ChunkStatus.FULL, false);
                    if (neighborChunk != null) {
                        chunkConnector.onNeighborChunkLoaded(side);
                        ((IBlockConnectableChunk) neighborChunk).multiconnect_getChunkConnector().onNeighborChunkLoaded(side.getOpposite());
                    }
                }
            }
        }
        return chunk;
    }

    @Dynamic
    @Inject(method = "method_31176", remap = false, at = @At("RETURN"))
    private void fixDeltaChunk(int flags, BlockPos pos, BlockState state, CallbackInfo ci) {
        Chunk chunk = world.getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.FULL, false);
        if (chunk != null) {
            ChunkConnector connector = ((IBlockConnectableChunk) chunk).multiconnect_getChunkConnector();
            if (connector != null) {
                connector.onBlockChange(pos, state.getBlock(), true);
            }
        }
    }

    @Inject(method = "onGameJoin", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift = At.Shift.AFTER))
    private void onOnGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
        RegistryMutator mutator = new RegistryMutator();
        DynamicRegistryManager.Impl registries = (DynamicRegistryManager.Impl) packet.getRegistryManager();
        //noinspection ConstantConditions
        DynamicRegistryManagerImplAccessor registriesAccessor = (DynamicRegistryManagerImplAccessor) (Object) registries;
        registriesAccessor.setRegistries(new HashMap<>(registriesAccessor.getRegistries())); // make registries mutable
        ConnectionInfo.protocol.mutateDynamicRegistries(mutator, registries);

        for (RegistryKey<? extends Registry<?>> registryKey : DynamicRegistryManagerAccessor.getInfos().keySet()) {
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
                if (!dynamicRegistry.getOrEmpty(key).isPresent()) {
                    iregistry.register(val, iregistry.getNextId(), key, false);
                }
            });
        }
    }

    @Inject(method = "onSynchronizeTags", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift = At.Shift.AFTER))
    private void onOnSynchronizeTags(SynchronizeTagsS2CPacket packet, CallbackInfo ci) {
        TagRegistry<Block> blockTagRegistry = new TagRegistry<>();
        TagGroup<Block> blockTags = setExtraTags("block", packet.getTagManager().getBlocks(), blockTagRegistry, BlockTags.getRequiredTags(), ConnectionInfo.protocol::addExtraBlockTags);
        TagGroup<Item> itemTags = setExtraTags("item", packet.getTagManager().getItems(), new TagRegistry<>(), ItemTags.getRequiredTags(), itemTagRegistry -> ConnectionInfo.protocol.addExtraItemTags(itemTagRegistry, blockTagRegistry));
        TagGroup<Fluid> fluidTags = setExtraTags("fluid", packet.getTagManager().getFluids(), new TagRegistry<>(), FluidTags.getRequiredTags(), ConnectionInfo.protocol::addExtraFluidTags);
        TagGroup<EntityType<?>> entityTypeTags = setExtraTags("entity type", packet.getTagManager().getEntityTypes(), new TagRegistry<>(), EntityTypeTags.getRequiredTags(), ConnectionInfo.protocol::addExtraEntityTags);
        ((SynchronizeTagsS2CAccessor) packet).setTagManager(TagManager.create(blockTags, itemTags, fluidTags, entityTypeTags));
    }

    @Unique
    private static <T> TagGroup<T> setExtraTags(String type, TagGroup<T> group, TagRegistry<T> tagRegistry, List<? extends Tag.Identified<T>> requiredTags, Consumer<TagRegistry<T>> tagsAdder) {
        group.getTags().forEach((id, tag) -> tagRegistry.put(id, new HashSet<>(tag.values())));
        tagsAdder.accept(tagRegistry);
        BiMap<Identifier, Tag<T>> tagBiMap = HashBiMap.create(tagRegistry.size());
        tagRegistry.forEach((id, set) -> tagBiMap.put(id, Tag.of(set)));

        // ViaVersion doesn't send all required tags to older clients because they didn't check them. We have to add empty ones to substitute.
        if (ConnectionInfo.protocolVersion <= Protocols.V1_16_1) {
            List<Tag.Identified<T>> missingTags = new ArrayList<>(requiredTags);
            missingTags.removeIf(tag -> tagBiMap.containsKey(tag.getId()));
            if (!missingTags.isEmpty()) {
                MULTICONNECT_LOGGER.warn("Server didn't send required {} tags, adding empty substitutes for {}", type, missingTags);
                for (Tag.Identified<T> missingTag : missingTags) {
                    tagBiMap.put(missingTag.getId(), SetTag.empty());
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

}
