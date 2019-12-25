package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.world.chunk.IdListPalette;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IdListPalette.class)
public class MixinIdListPalette {

    @Inject(method = "fromPacket", at = @At("HEAD"))
    private void onFromPacket(PacketByteBuf buf, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2) {
            buf.readVarInt();
        }
    }

}
