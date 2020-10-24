package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemFrameEntity.class)
public interface ItemFrameEntityAccessor {
    @Accessor("ITEM_STACK")
    static TrackedData<ItemStack> getItemStack() {
        return MixinHelper.fakeInstance();
    }

    @Accessor("ROTATION")
    static TrackedData<Integer> getRotation() {
        return MixinHelper.fakeInstance();
    }
}
