package net.earthcomputer.multiconnect.protocols.v1_13_2.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.entity.item.EyeOfEnderEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EyeOfEnderEntity.class)
public interface EnderEyeEntityAccessor {
    @Accessor("field_213864_b")
    static DataParameter<ItemStack> getItem() {
        return MixinHelper.fakeInstance();
    }
}
