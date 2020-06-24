package net.earthcomputer.multiconnect.mixin.bridge;

import net.earthcomputer.multiconnect.transformer.ChunkData;
import net.earthcomputer.multiconnect.transformer.TransformerByteBuf;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkDataS2CPacket.class)
public class MixinChunkDataS2C {

    @Inject(method = "getReadBuffer", at = @At("RETURN"), cancellable = true)
    private void onGetReadBuffer(CallbackInfoReturnable<PacketByteBuf> ci) {
        TransformerByteBuf transformerByteBuf = new TransformerByteBuf(ci.getReturnValue(), null);
        transformerByteBuf.readTopLevelType(ChunkData.class);
        ci.setReturnValue(transformerByteBuf);
    }

}
