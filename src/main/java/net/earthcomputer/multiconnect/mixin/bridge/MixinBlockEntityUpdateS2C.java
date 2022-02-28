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
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityUpdateS2CPacket.class)
public class MixinBlockEntityUpdateS2C {

    @Shadow @Final private BlockEntityType<?> blockEntityType;

    @Shadow @Final @Mutable @Nullable
    private NbtCompound nbt;

    @Inject(method = "<init>(Lnet/minecraft/network/PacketByteBuf;)V", at = @At("RETURN"))
    private void onRead(CallbackInfo ci) {
        if (!MultiConnectAPI.instance().doesServerKnow(Registry.BLOCK_ENTITY_TYPE, blockEntityType)
                || DefaultRegistries.getDefaultRegistry(Registry.BLOCK_ENTITY_TYPE_KEY).getKey(blockEntityType).isEmpty()) {
            return;
        }

        if (nbt != null) {
            nbt.putString("id", ConnectionInfo.protocolVersion <= Protocols.V1_10 ? Protocol_1_10.getBlockEntityId(blockEntityType) : String.valueOf(Registry.BLOCK_ENTITY_TYPE.getId(blockEntityType)));
            nbt = Utils.datafix(TypeReferences.BLOCK_ENTITY, nbt);
        }
    }

}
