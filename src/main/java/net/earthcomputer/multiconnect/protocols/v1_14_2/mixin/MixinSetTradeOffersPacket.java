package net.earthcomputer.multiconnect.protocols.v1_14_2.mixin;

import net.earthcomputer.multiconnect.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.Protocols;
import net.minecraft.client.network.packet.SetTradeOffersPacket;
import net.minecraft.util.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SetTradeOffersPacket.class)
public class MixinSetTradeOffersPacket {

    @Redirect(method = "read", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/PacketByteBuf;readBoolean()Z", ordinal = 1))
    public boolean redirectReadRefreshable(PacketByteBuf buf) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_14_2)
            return true;
        else
            return buf.readBoolean();
    }

}
