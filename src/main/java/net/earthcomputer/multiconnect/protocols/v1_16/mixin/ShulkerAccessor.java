package net.earthcomputer.multiconnect.protocols.v1_16.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.monster.Shulker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Shulker.class)
public interface ShulkerAccessor {
    @Accessor("DATA_PEEK_ID")
    static EntityDataAccessor<Byte> getDataPeekId() {
        return MixinHelper.fakeInstance();
    }
}
