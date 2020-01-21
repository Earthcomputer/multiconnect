package net.earthcomputer.multiconnect.protocols.v1_13_2.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.entity.EnderEyeEntity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EnderEyeEntity.class)
public interface EnderEyeEntityAccessor {
    @Accessor("ITEM")
    static TrackedData<ItemStack> getItem() {
        return MixinHelper.fakeInstance();
    }
}
