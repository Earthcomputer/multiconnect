package net.earthcomputer.multiconnect.protocols.v1_14_4.mixin;

import net.earthcomputer.multiconnect.protocols.v1_14_4.ITrackerUpdatePacket;
import net.minecraft.client.network.packet.EntityTrackerUpdateS2CPacket;
import net.minecraft.entity.data.DataTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(EntityTrackerUpdateS2CPacket.class)
public abstract class MixinTrackerUpdatePacket implements ITrackerUpdatePacket {

    @Accessor
    @Override
    public abstract void setId(int id);

    @Accessor
    @Override
    public abstract void setTrackedValues(List<DataTracker.Entry<?>> entries);
}
