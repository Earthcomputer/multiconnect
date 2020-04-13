package net.earthcomputer.multiconnect.mixin;

import net.earthcomputer.multiconnect.transformer.CustomPayload;
import net.earthcomputer.multiconnect.transformer.TransformerByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SCustomPayloadPlayPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SCustomPayloadPlayPacket.class)
public class MixinCustomPayloadS2C {

    @Inject(method = "getBufferData", at = @At("RETURN"), cancellable = true)
    private void onRead(CallbackInfoReturnable<PacketBuffer> ci) {
        ci.setReturnValue(new TransformerByteBuf(ci.getReturnValue(), null).readTopLevelType(CustomPayload.class));
    }

}
