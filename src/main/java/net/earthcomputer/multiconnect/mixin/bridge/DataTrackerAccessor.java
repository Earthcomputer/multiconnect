package net.earthcomputer.multiconnect.mixin.bridge;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.network.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(DataTracker.class)
public interface DataTrackerAccessor {
    @Invoker
    static <T> DataTracker.Entry<T> callEntryFromPacket(PacketByteBuf buf, int id, TrackedDataHandler<T> handler) {
        return MixinHelper.fakeInstance();
    }
}
