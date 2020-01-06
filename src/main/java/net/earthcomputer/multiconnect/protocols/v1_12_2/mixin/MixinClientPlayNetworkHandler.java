package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.v1_12_2.FlowerPotBlockEntity;
import net.earthcomputer.multiconnect.protocols.v1_12_2.IRegistryTagManager;
import net.earthcomputer.multiconnect.protocols.v1_12_2.ITagContainer;
import net.earthcomputer.multiconnect.protocols.v1_12_2.Protocol_1_12_2;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.packet.BlockEntityUpdateS2CPacket;
import net.minecraft.client.network.packet.GameJoinS2CPacket;
import net.minecraft.client.network.packet.SynchronizeTagsS2CPacket;
import net.minecraft.tag.RegistryTagContainer;
import net.minecraft.tag.RegistryTagManager;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.Map;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler {

    @Shadow private MinecraftClient client;

    @Shadow public abstract void onSynchronizeTags(SynchronizeTagsS2CPacket packet);

    @Inject(method = "onGameJoin", at = @At("RETURN"))
    private void onOnGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2) {
            RegistryTagManager tagManager = new RegistryTagManager();
            //noinspection ConstantConditions
            IRegistryTagManager iTagManager = (IRegistryTagManager) tagManager;
            Protocol_1_12_2 protocol = (Protocol_1_12_2) ConnectionInfo.protocol;
            toTagContainer(iTagManager.getBlocks(), protocol.getBlockTags());
            toTagContainer(iTagManager.getItems(), protocol.getItemTags());
            toTagContainer(iTagManager.getFluids(), protocol.getFluidTags());
            toTagContainer(iTagManager.getEntityTypes(), protocol.getEntityTypeTags());
            onSynchronizeTags(new SynchronizeTagsS2CPacket(tagManager));
        }
    }

    @SuppressWarnings("unchecked")
    @Unique
    private <T> void toTagContainer(RegistryTagContainer<T> container, Multimap<Tag<T>, T> tags) {
        ImmutableMap.Builder<Identifier, Tag<T>> map = new ImmutableMap.Builder<>();
        for (Map.Entry<Tag<T>, Collection<T>> entry : tags.asMap().entrySet()) {
            Identifier id = entry.getKey().getId();
            Tag.Builder<T> tag = Tag.Builder.create();
            entry.getValue().forEach(tag::add);
            map.put(id, tag.build(id));
        }
        ((ITagContainer<T>) container).multiconnect_setEntries(map.build());
    }

    @Inject(method = "onBlockEntityUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/packet/BlockEntityUpdateS2CPacket;getBlockEntityType()I"))
    private void onOnBlockEntityUpdate(BlockEntityUpdateS2CPacket packet, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2) {
            assert client.world != null;
            BlockEntity be = client.world.getBlockEntity(packet.getPos());
            if (packet.getBlockEntityType() == 5 && be instanceof FlowerPotBlockEntity) {
                be.fromTag(packet.getCompoundTag());
            }
        }
    }

}
