package net.earthcomputer.multiconnect.protocols.v1_13_2.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.entity.EntityType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.NetworkTagCollection;
import net.minecraft.tags.NetworkTagManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(NetworkTagManager.class)
public class MixinRegistryTagManager {

    @Redirect(method = "read", at = @At(value = "INVOKE", target = "Lnet/minecraft/tags/NetworkTagCollection;read(Lnet/minecraft/network/PacketBuffer;)V", ordinal = 3))
    private static void redirectEntityFromPacket(NetworkTagCollection<EntityType<?>> tagContainer, PacketBuffer buf) {
        if (ConnectionInfo.protocolVersion > Protocols.V1_13_2)
            tagContainer.read(buf);
    }

}
