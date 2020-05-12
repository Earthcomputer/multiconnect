package net.earthcomputer.multiconnect.mixin;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.impl.CurrentChunkDataPacket;
import net.earthcomputer.multiconnect.impl.TagRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.SynchronizeTagsS2CPacket;
import net.minecraft.tag.Tag;
import net.minecraft.tag.TagContainer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.function.Consumer;

@Mixin(ClientPlayNetworkHandler.class)
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

}
