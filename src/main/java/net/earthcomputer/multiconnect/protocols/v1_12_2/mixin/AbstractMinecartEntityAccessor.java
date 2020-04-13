package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.network.datasync.DataParameter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractMinecartEntity.class)
public interface AbstractMinecartEntityAccessor {
    @Accessor("DISPLAY_TILE")
    static DataParameter<Integer> getCustomBlockId() {
        return MixinHelper.fakeInstance();
    }
}
