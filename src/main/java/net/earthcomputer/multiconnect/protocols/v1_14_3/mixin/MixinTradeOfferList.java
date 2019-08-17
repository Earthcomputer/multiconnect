package net.earthcomputer.multiconnect.protocols.v1_14_3.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.village.TraderOfferList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TraderOfferList.class)
public class MixinTradeOfferList {

    @Redirect(method = "fromPacket", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/PacketByteBuf;readInt()I", ordinal = 4))
    private static int redirectReadDemandBonus(PacketByteBuf buf) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_14_3)
            return 0;
        else
            return buf.readInt();
    }

}
