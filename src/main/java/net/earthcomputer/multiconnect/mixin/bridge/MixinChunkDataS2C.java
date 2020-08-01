package net.earthcomputer.multiconnect.mixin.bridge;

import net.earthcomputer.multiconnect.impl.Utils;
import net.earthcomputer.multiconnect.transformer.ChunkData;
import net.earthcomputer.multiconnect.transformer.TransformerByteBuf;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ChunkDataS2CPacket.class)
public class MixinChunkDataS2C {

    @Shadow private List<CompoundTag> blockEntities;

    @Inject(method = "getReadBuffer", at = @At("RETURN"), cancellable = true)
    private void onGetReadBuffer(CallbackInfoReturnable<PacketByteBuf> ci) {
        TransformerByteBuf transformerByteBuf = new TransformerByteBuf(ci.getReturnValue(), null);
        transformerByteBuf.readTopLevelType(ChunkData.class);
        ci.setReturnValue(transformerByteBuf);
    }

    @Inject(method = "read", at = @At("RETURN"))
    private void onRead(CallbackInfo ci) {
        for (int i = 0; i < blockEntities.size(); i++) {
            blockEntities.set(i, Utils.datafix(TypeReferences.BLOCK_ENTITY, blockEntities.get(i)));
        }
    }

}
