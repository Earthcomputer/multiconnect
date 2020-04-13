package net.earthcomputer.multiconnect.protocols.v1_13_2.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.entity.merchant.villager.VillagerData;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.network.datasync.DataParameter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(VillagerEntity.class)
public interface VillagerEntityAccessor {
    @Accessor("VILLAGER_DATA")
    static DataParameter<VillagerData> getVillagerData() {
        return MixinHelper.fakeInstance();
    }
}
