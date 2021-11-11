package net.earthcomputer.multiconnect.mixin.bridge;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.impl.Utils;
import net.earthcomputer.multiconnect.protocols.generic.DefaultRegistries;
import net.earthcomputer.multiconnect.protocols.v1_10.Protocol_1_10;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.class_6603;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_6603.class_6604.class)
public abstract class MixinChunkDataBlockEntity {
    @Shadow @Final BlockEntityType<?> field_34868;
    @Shadow @Final @Mutable @Nullable NbtCompound field_34869;

    @Inject(method = "<init>(Lnet/minecraft/network/PacketByteBuf;)V", at = @At("RETURN"))
    private void onRead(CallbackInfo ci) {
        DefaultRegistries<?> defaultBlockEntities = DefaultRegistries.DEFAULT_REGISTRIES.get(Registry.BLOCK_ENTITY_TYPE);
        if (field_34869 != null) {
            if (defaultBlockEntities.defaultEntryToRawId.containsKey(field_34868)) {
                NbtCompound fixed = field_34869;
                if (ConnectionInfo.protocolVersion <= Protocols.V1_10) {
                    fixed.putString("id", Protocol_1_10.getBlockEntityId(field_34868));
                } else {
                    fixed.putString("id", String.valueOf(Registry.BLOCK_ENTITY_TYPE.getId(field_34868)));
                }
                fixed = Utils.datafix(TypeReferences.BLOCK_ENTITY, fixed);
                field_34869 = fixed;
            }
        }
    }
}
