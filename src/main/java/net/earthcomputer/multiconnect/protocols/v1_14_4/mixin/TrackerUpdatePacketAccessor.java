package net.earthcomputer.multiconnect.protocols.v1_14_4.mixin;

import net.minecraft.entity.data.DataTracker;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(EntityTrackerUpdateS2CPacket.class)
public interface TrackerUpdatePacketAccessor {

    @Accessor
    void setId(int id);

    @Accessor
    void setTrackedValues(List<DataTracker.Entry<?>> entries);

}
