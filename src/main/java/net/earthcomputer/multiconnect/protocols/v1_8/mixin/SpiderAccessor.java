package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.monster.Spider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Spider.class)
public interface SpiderAccessor {
    @Accessor("DATA_FLAGS_ID")
    static EntityDataAccessor<Byte> getDataFlagsId() {
        return MixinHelper.fakeInstance();
    }
}
