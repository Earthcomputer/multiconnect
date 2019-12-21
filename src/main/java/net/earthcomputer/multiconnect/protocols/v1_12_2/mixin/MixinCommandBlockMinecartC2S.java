package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import net.earthcomputer.multiconnect.protocols.v1_12_2.ICommandBlockMinecartC2SPacket;
import net.minecraft.server.network.packet.UpdateCommandBlockMinecartC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(UpdateCommandBlockMinecartC2SPacket.class)
public abstract class MixinCommandBlockMinecartC2S implements ICommandBlockMinecartC2SPacket {

    @Accessor
    @Override
    public abstract int getEntityId();

}
