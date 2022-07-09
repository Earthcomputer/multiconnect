package net.earthcomputer.multiconnect.mixin.bridge;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.SynchedEntityData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SynchedEntityData.class)
public interface DataTrackerAccessor {
    @Invoker("genericHelper")
    static <T> SynchedEntityData.DataItem<T> callEntryFromPacket(FriendlyByteBuf buf, int id, EntityDataSerializer<T> handler) {
        return MixinHelper.fakeInstance();
    }
}
