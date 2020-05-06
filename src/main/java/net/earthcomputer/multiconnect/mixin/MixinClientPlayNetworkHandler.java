package net.earthcomputer.multiconnect.mixin;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.impl.CurrentChunkDataPacket;
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

import java.util.Map;
import java.util.Set;

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
        setExtraTags(packet.getTagManager().blocks(), ConnectionInfo.protocol.getExtraBlockTags());
        setExtraTags(packet.getTagManager().items(), ConnectionInfo.protocol.getExtraItemTags());
        setExtraTags(packet.getTagManager().fluids(), ConnectionInfo.protocol.getExtraFluidTags());
        setExtraTags(packet.getTagManager().entityTypes(), ConnectionInfo.protocol.getExtraEntityTags());
    }

    @SuppressWarnings("unchecked")
    @Unique
    private static <T> void setExtraTags(TagContainer<T> container, Map<Tag.Identified<T>, Set<T>> extraTags) {
        if (extraTags.isEmpty()) {
            return;
        }
        TagContainerAccessor<T> accessor = (TagContainerAccessor<T>) container;
        BiMap<Identifier, Tag<T>> tags = HashBiMap.create(accessor.multiconnect_getEntries());
        for (Tag.Identified<T> tag : extraTags.keySet()) {
            tags.put(tag.getId(), Tag.of(extraTags.get(tag)));
        }
        accessor.multiconnect_setEntries(tags);
    }

}
