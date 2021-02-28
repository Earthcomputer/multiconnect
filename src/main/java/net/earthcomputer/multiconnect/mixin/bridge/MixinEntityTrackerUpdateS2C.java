package net.earthcomputer.multiconnect.mixin.bridge;

import net.earthcomputer.multiconnect.protocols.generic.DataTrackerManager;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(EntityTrackerUpdateS2CPacket.class)
public class MixinEntityTrackerUpdateS2C {
    @Redirect(method = "<init>(Lnet/minecraft/network/PacketByteBuf;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/data/DataTracker;deserializePacket(Lnet/minecraft/network/PacketByteBuf;)Ljava/util/List;"))
    private List<DataTracker.Entry<?>> redirectDeserializePacket(PacketByteBuf buf) {
        return DataTrackerManager.deserializePacket(buf);
    }
}
