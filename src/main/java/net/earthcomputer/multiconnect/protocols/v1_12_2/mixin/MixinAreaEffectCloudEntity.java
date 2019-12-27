package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import net.earthcomputer.multiconnect.protocols.v1_12_2.IAreaEffectCloudEntity;
import net.minecraft.entity.AreaEffectCloudEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AreaEffectCloudEntity.class)
public class MixinAreaEffectCloudEntity implements IAreaEffectCloudEntity {

    @Unique private int param1;
    @Unique private int param2;

    @Override
    public int multiconnect_getParam1() {
        return param1;
    }

    @Override
    public void multiconnect_setParam1(int param1) {
        this.param1 = param1;
    }

    @Override
    public int multiconnect_getParam2() {
        return param2;
    }

    @Override
    public void multiconnect_setParam2(int param2) {
        this.param2 = param2;
    }
}
