package net.earthcomputer.multiconnect.mixin.bridge;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.generic.CurrentChunkDataPacket;
import net.earthcomputer.multiconnect.protocols.generic.CustomPayloadHandler;
import net.earthcomputer.multiconnect.protocols.generic.TagRegistry;
import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.class_5414;
import net.minecraft.class_5415;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.play.SynchronizeTagsS2CPacket;
import net.minecraft.tag.*;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.function.Consumer;

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
        TagRegistry<Block> blockTagRegistry = new TagRegistry<>();
        class_5414<Block> blockTags = setExtraTags(packet.getTagManager().method_30215(), blockTagRegistry, ConnectionInfo.protocol::addExtraBlockTags);
        class_5414<Item> itemTags = setExtraTags(packet.getTagManager().method_30218(), new TagRegistry<>(), itemTagRegistry -> ConnectionInfo.protocol.addExtraItemTags(itemTagRegistry, blockTagRegistry));
        class_5414<Fluid> fluidTags = setExtraTags(packet.getTagManager().method_30220(), new TagRegistry<>(), ConnectionInfo.protocol::addExtraFluidTags);
        class_5414<EntityType<?>> entityTypeTags = setExtraTags(packet.getTagManager().method_30221(), new TagRegistry<>(), ConnectionInfo.protocol::addExtraEntityTags);
        ((SynchronizeTagsS2CAccessor) packet).setTagManager(class_5415.method_30216(blockTags, itemTags, fluidTags, entityTypeTags));
    }

    @Unique
    private static <T> class_5414<T> setExtraTags(class_5414<T> container, TagRegistry<T> tagRegistry, Consumer<TagRegistry<T>> tagsAdder) {
        container.method_30204().forEach((id, tag) -> tagRegistry.put(id, new HashSet<>(tag.values())));
        tagsAdder.accept(tagRegistry);
        BiMap<Identifier, Tag<T>> tagBiMap = HashBiMap.create(tagRegistry.size());
        tagRegistry.forEach((id, set) -> tagBiMap.put(id, Tag.of(set)));
        return class_5414.method_30207(tagBiMap);
    }

    @Inject(method = "onCustomPayload", at = @At("HEAD"), cancellable = true)
    private void onOnCustomPayload(CustomPayloadS2CPacket packet, CallbackInfo ci) {
        NetworkThreadUtils.forceMainThread(packet, (ClientPlayNetworkHandler) (Object) this, MinecraftClient.getInstance());
        if (packet.getChannel().equals(CustomPayloadHandler.DROP_ID)) {
            ci.cancel();
        } else if (ConnectionInfo.protocolVersion != SharedConstants.getGameVersion().getProtocolVersion()
                && !CustomPayloadHandler.VANILLA_CHANNELS.contains(packet.getChannel())) {
            CustomPayloadHandler.handleCustomPayload(packet);
            ci.cancel();
        }
    }

}
