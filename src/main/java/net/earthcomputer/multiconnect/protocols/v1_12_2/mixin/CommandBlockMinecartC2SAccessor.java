package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import net.minecraft.network.play.client.CUpdateMinecartCommandBlockPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CUpdateMinecartCommandBlockPacket.class)
public interface CommandBlockMinecartC2SAccessor {

    @Accessor
    int getEntityId();

}
