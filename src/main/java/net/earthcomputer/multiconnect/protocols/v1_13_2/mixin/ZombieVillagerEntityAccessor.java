package net.earthcomputer.multiconnect.protocols.v1_13_2.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.entity.merchant.villager.VillagerData;
import net.minecraft.entity.monster.ZombieVillagerEntity;
import net.minecraft.network.datasync.DataParameter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ZombieVillagerEntity.class)
public interface ZombieVillagerEntityAccessor {
    @Accessor("field_213795_c")
    static DataParameter<VillagerData> getVillagerData() {
        return MixinHelper.fakeInstance();
    }
}
