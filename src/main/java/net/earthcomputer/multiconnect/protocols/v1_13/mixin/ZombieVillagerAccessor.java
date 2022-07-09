package net.earthcomputer.multiconnect.protocols.v1_13.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.VillagerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ZombieVillager.class)
public interface ZombieVillagerAccessor {
    @Accessor("DATA_VILLAGER_DATA")
    static EntityDataAccessor<VillagerData> getDataVillagerData() {
        return MixinHelper.fakeInstance();
    }
}
