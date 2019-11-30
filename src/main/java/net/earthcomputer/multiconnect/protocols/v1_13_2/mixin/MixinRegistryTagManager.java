package net.earthcomputer.multiconnect.protocols.v1_13_2.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.entity.EntityType;
import net.minecraft.tag.RegistryTagContainer;
import net.minecraft.tag.RegistryTagManager;
import net.minecraft.util.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RegistryTagManager.class)
public class MixinRegistryTagManager {

    @Redirect(method = "fromPacket", at = @At(value = "INVOKE", target = "Lnet/minecraft/tag/RegistryTagContainer;fromPacket(Lnet/minecraft/util/PacketByteBuf;)V", ordinal = 3))
    private static void redirectEntityFromPacket(RegistryTagContainer<EntityType<?>> tagContainer, PacketByteBuf buf) {
        if (ConnectionInfo.protocolVersion > Protocols.V1_13_2)
            tagContainer.fromPacket(buf);
    }

}
