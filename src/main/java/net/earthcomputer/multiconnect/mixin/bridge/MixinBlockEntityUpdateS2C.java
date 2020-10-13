package net.earthcomputer.multiconnect.mixin.bridge;

import net.earthcomputer.multiconnect.api.MultiConnectAPI;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.impl.Utils;
import net.earthcomputer.multiconnect.protocols.v1_10.Protocol_1_10;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityUpdateS2CPacket.class)
public class MixinBlockEntityUpdateS2C {

    @Shadow private int blockEntityType;

    @Shadow private CompoundTag tag;

    @Inject(method = "read", at = @At("RETURN"))
    private void onRead(CallbackInfo ci) {
        BlockEntityType<?> type;
        switch (blockEntityType) {
            case 1: type = BlockEntityType.MOB_SPAWNER; break;
            case 2: type = BlockEntityType.COMMAND_BLOCK; break;
            case 3: type = BlockEntityType.BEACON; break;
            case 4: type = BlockEntityType.SKULL; break;
            case 5: type = BlockEntityType.CONDUIT; break;
            case 6: type = BlockEntityType.BANNER; break;
            case 7: type = BlockEntityType.STRUCTURE_BLOCK; break;
            case 8: type = BlockEntityType.END_GATEWAY; break;
            case 9: type = BlockEntityType.SIGN; break;
            case 11: type = BlockEntityType.BED; break;
            case 12: type = BlockEntityType.JIGSAW; break;
            case 13: type = BlockEntityType.CAMPFIRE; break;
            case 14: type = BlockEntityType.BEEHIVE; break;
            default: return;
        }
        if (!MultiConnectAPI.instance().doesServerKnow(Registry.BLOCK_ENTITY_TYPE, type)) return;

        tag.putString("id", ConnectionInfo.protocolVersion <= Protocols.V1_10 ? Protocol_1_10.getBlockEntityId(type) : String.valueOf(Registry.BLOCK_ENTITY_TYPE.getId(type)));
        tag = Utils.datafix(TypeReferences.BLOCK_ENTITY, tag);
    }

}
