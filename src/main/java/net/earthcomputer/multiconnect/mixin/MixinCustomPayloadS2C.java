package net.earthcomputer.multiconnect.mixin;

import net.earthcomputer.multiconnect.transformer.CustomPayload;
import net.earthcomputer.multiconnect.transformer.TransformerByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.util.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CustomPayloadS2CPacket.class)
public class MixinCustomPayloadS2C {

    @Inject(method = "getData", at = @At("RETURN"), cancellable = true)
    private void onRead(CallbackInfoReturnable<PacketByteBuf> ci) {
        ci.setReturnValue(new TransformerByteBuf(ci.getReturnValue(), null).readTopLevelType(CustomPayload.class));
    }

}
