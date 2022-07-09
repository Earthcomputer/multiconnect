package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemFrame.class)
public interface ItemFrameAccessor {
    @Accessor("DATA_ITEM")
    static EntityDataAccessor<ItemStack> getDataItem() {
        return MixinHelper.fakeInstance();
    }

    @Accessor("DATA_ROTATION")
    static EntityDataAccessor<Integer> getDataRotation() {
        return MixinHelper.fakeInstance();
    }
}
