package net.earthcomputer.multiconnect.mixin.bridge;

import net.earthcomputer.multiconnect.protocols.generic.ICustomPayloadC2SPacket;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CustomPayloadC2SPacket.class)
public abstract class MixinCustomPayloadC2S implements ICustomPayloadC2SPacket {
    @Unique
    private boolean blocked;

    @Inject(method = "<init>(Lnet/minecraft/util/Identifier;Lnet/minecraft/network/PacketByteBuf;)V", at = @At("RETURN"))
    private void onInit(Identifier channel, PacketByteBuf data, CallbackInfo ci) {
        this.blocked = !CustomPayloadC2SPacket.BRAND.equals(channel);
    }

    @Override
    public boolean multiconnect_isBlocked() {
        return blocked;
    }

    @Override
    public void multiconnect_unblock() {
        this.blocked = false;
    }
}
