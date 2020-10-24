package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityPassengersSetS2CPacket.class)
public interface EntityPassengersSetS2CAccessor {
    @Accessor
    void setId(int id);

    @Accessor
    void setPassengerIds(int[] passengerIds);
}
