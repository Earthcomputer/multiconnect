package net.earthcomputer.multiconnect.mixin.bridge;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.generic.CurrentChunkDataPacket;
import net.earthcomputer.multiconnect.protocols.generic.CustomPayloadHandler;
import net.earthcomputer.multiconnect.protocols.generic.TagRegistry;
import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.play.SynchronizeTagsS2CPacket;
import net.minecraft.tag.*;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Mixin(value = ClientPlayNetworkHandler.class, priority = -1000)
public class MixinClientPlayNetworkHandler {

    @Inject(method = "onChunkData", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift = At.Shift.AFTER))
    private void preChunkData(ChunkDataS2CPacket packet, CallbackInfo ci) {
        CurrentChunkDataPacket.push(packet);
    }

    @Inject(method = "onChunkData", at = @At("RETURN"))
    private void postChunkData(ChunkDataS2CPacket packet, CallbackInfo ci) {
        CurrentChunkDataPacket.pop();
    }

    @Inject(method = "onSynchronizeTags", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift = At.Shift.AFTER))
    private void onOnSynchronizeTags(SynchronizeTagsS2CPacket packet, CallbackInfo ci) {
        TagRegistry<Block> blockTags = setExtraTags(packet.getTagManager().blocks(), ConnectionInfo.protocol::addExtraBlockTags);
        setExtraTags(packet.getTagManager().items(), itemTags -> ConnectionInfo.protocol.addExtraItemTags(itemTags, blockTags));
        setExtraTags(packet.getTagManager().fluids(), ConnectionInfo.protocol::addExtraFluidTags);
        setExtraTags(packet.getTagManager().entityTypes(), ConnectionInfo.protocol::addExtraEntityTags);
        checkRequiredTags(packet.getTagManager());
    }

    @SuppressWarnings("unchecked")
    @Unique
    private static <T> TagRegistry<T> setExtraTags(TagContainer<T> container, Consumer<TagRegistry<T>> tagsAdder) {
        TagContainerAccessor<T> accessor = (TagContainerAccessor<T>) container;
        TagRegistry<T> tags = new TagRegistry<>();
        container.getEntries().forEach((id, tag) -> tags.put(id, new HashSet<>(tag.values())));
        tagsAdder.accept(tags);
        BiMap<Identifier, Tag<T>> tagBiMap = HashBiMap.create(tags.size());
        tags.forEach((id, set) -> tagBiMap.put(id, Tag.of(set)));
        accessor.multiconnect_setEntries(tagBiMap);
        return tags;
    }

    @Unique
    private static void checkRequiredTags(RegistryTagManager tagManager) {
        Multimap<String, Identifier> missingTags = HashMultimap.create();
        missingTags.putAll("blocks", BlockTags.method_29214(tagManager.blocks()));
        missingTags.putAll("items", ItemTags.method_29217(tagManager.items()));
        missingTags.putAll("fluids", FluidTags.method_29216(tagManager.fluids()));
        missingTags.putAll("entity_types", EntityTypeTags.method_29215(tagManager.entityTypes()));
        if (!missingTags.isEmpty()) {
            LogManager.getLogger("multiconnect").error("Missing required tags: " + missingTags.entries().stream()
                    .map(entry -> entry.getKey() + ":" + entry.getValue())
                    .sorted()
                    .collect(Collectors.joining(",")));
        }
    }

    @Inject(method = "onCustomPayload", at = @At("HEAD"), cancellable = true)
    private void onOnCustomPayload(CustomPayloadS2CPacket packet, CallbackInfo ci) {
        NetworkThreadUtils.forceMainThread(packet, (ClientPlayNetworkHandler) (Object) this, MinecraftClient.getInstance());
        if (packet.getChannel().equals(CustomPayloadHandler.DROP_ID)) {
            ci.cancel();
        } else if (ConnectionInfo.protocolVersion != SharedConstants.getGameVersion().getProtocolVersion()
                && !CustomPayloadHandler.VANILLA_CLIENTBOUND_CHANNELS.contains(packet.getChannel())) {
            CustomPayloadHandler.handleClientboundCustomPayload(packet);
            ci.cancel();
        }
    }

}
