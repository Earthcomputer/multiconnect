package net.earthcomputer.multiconnect.mixin.bridge;

import net.earthcomputer.multiconnect.protocols.generic.SynchedDataManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(ClientboundSetEntityDataPacket.class)
public class ClientboundSetEntityDataPacketMixin {
    @Redirect(method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/syncher/SynchedEntityData;unpack(Lnet/minecraft/network/FriendlyByteBuf;)Ljava/util/List;"))
    private List<SynchedEntityData.DataItem<?>> redirectDeserializePacket(FriendlyByteBuf buf) {
        return SynchedDataManager.deserializePacket(buf);
    }
}
