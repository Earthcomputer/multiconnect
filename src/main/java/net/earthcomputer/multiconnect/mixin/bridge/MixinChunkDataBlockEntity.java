package net.earthcomputer.multiconnect.mixin.bridge;

import net.earthcomputer.multiconnect.api.MultiConnectAPI;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.impl.Utils;
import net.earthcomputer.multiconnect.protocols.generic.DefaultRegistries;
import net.earthcomputer.multiconnect.protocols.v1_10.Protocol_1_10;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.ChunkData;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkData.BlockEntityData.class)
public abstract class MixinChunkDataBlockEntity {
    @Shadow @Final BlockEntityType<?> type;
    @Shadow @Final @Mutable @Nullable NbtCompound nbt;

    @Inject(method = "<init>(Lnet/minecraft/network/PacketByteBuf;)V", at = @At("RETURN"))
    private void onRead(CallbackInfo ci) {
        Registry<BlockEntityType<?>> defaultBlockEntities = DefaultRegistries.getDefaultRegistry(Registry.BLOCK_ENTITY_TYPE_KEY);
        if (nbt != null) {
            if (defaultBlockEntities.getKey(type).isPresent()) {
                NbtCompound fixed = nbt;
                if (ConnectionInfo.protocolVersion <= Protocols.V1_10) {
                    fixed.putString("id", Protocol_1_10.getBlockEntityId(type));
                } else {
                    fixed.putString("id", String.valueOf(Registry.BLOCK_ENTITY_TYPE.getId(type)));
                }
                if (MultiConnectAPI.instance().doesServerKnow(Registry.BLOCK_ENTITY_TYPE, type)
                        && DefaultRegistries.getDefaultRegistry(Registry.BLOCK_ENTITY_TYPE_KEY).getKey(type).isPresent()) {
                    fixed = Utils.datafix(TypeReferences.BLOCK_ENTITY, fixed);
                }
                nbt = fixed;
            }
        }
    }
}
