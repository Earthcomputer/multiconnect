package net.earthcomputer.multiconnect.mixin.bridge;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.impl.Utils;
import net.earthcomputer.multiconnect.protocols.generic.DefaultRegistries;
import net.earthcomputer.multiconnect.protocols.v1_10.Protocol_1_10;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ChunkDataS2CPacket.class)
public abstract class MixinChunkDataS2C {
    @Shadow @Final private List<NbtCompound> blockEntities;

    @Inject(method = "<init>(Lnet/minecraft/network/PacketByteBuf;)V", at = @At("RETURN"))
    private void onRead(CallbackInfo ci) {
        DefaultRegistries<?> defaultBlockEntities = DefaultRegistries.DEFAULT_REGISTRIES.get(Registry.BLOCK_ENTITY_TYPE);
        for (int i = 0; i < blockEntities.size(); i++) {
            NbtCompound blockEntity = blockEntities.get(i);
            BlockEntityType<?> blockEntityType;
            if (ConnectionInfo.protocolVersion <= Protocols.V1_10) {
                blockEntityType = Protocol_1_10.getBlockEntityById(blockEntity.getString("id"));
            } else {
                Identifier blockEntityId = Identifier.tryParse(blockEntity.getString("id"));
                blockEntityType = blockEntityId == null ? null : Registry.BLOCK_ENTITY_TYPE.getOrEmpty(blockEntityId).orElse(null);
            }
            if (blockEntityType != null) {
                if (defaultBlockEntities.defaultEntryToRawId.containsKey(blockEntityType)) {
                    NbtCompound fixed = Utils.datafix(TypeReferences.BLOCK_ENTITY, blockEntity);
                    fixed.putString("id", String.valueOf(Registry.BLOCK_ENTITY_TYPE.getId(blockEntityType)));
                    blockEntities.set(i, fixed);
                }
            }
        }
    }
}
