package net.earthcomputer.multiconnect.protocols.v1_14_4.mixin;

import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SEntityMetadataPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(SEntityMetadataPacket.class)
public interface TrackerUpdatePacketAccessor {

    @Accessor
    void setEntityId(int id);

    @Accessor
    void setDataManagerEntries(List<EntityDataManager.DataEntry<?>> entries);

}
