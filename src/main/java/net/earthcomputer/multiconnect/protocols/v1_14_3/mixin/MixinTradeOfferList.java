package net.earthcomputer.multiconnect.protocols.v1_14_3.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.item.MerchantOffers;
import net.minecraft.network.PacketBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MerchantOffers.class)
public class MixinTradeOfferList {

    @Redirect(method = "read", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketBuffer;readInt()I", ordinal = 4))
    private static int redirectReadDemandBonus(PacketBuffer buf) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_14_3)
            return 0;
        else
            return buf.readInt();
    }

}
