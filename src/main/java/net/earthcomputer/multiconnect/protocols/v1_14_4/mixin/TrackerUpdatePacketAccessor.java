package net.earthcomputer.multiconnect.protocols.v1_14_4.mixin;

import net.minecraft.client.network.packet.EntityTrackerUpdateS2CPacket;
import net.minecraft.entity.data.DataTracker;
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
