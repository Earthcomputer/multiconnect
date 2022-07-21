package net.earthcomputer.multiconnect.protocols.v1_12.mixin;

import net.earthcomputer.multiconnect.protocols.v1_12.IAreaEffectCloud;
import net.minecraft.world.entity.AreaEffectCloud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AreaEffectCloud.class)
public class AreaEffectCloudMixin implements IAreaEffectCloud {

    @Unique private int multiconnect_param1;
    @Unique private int multiconnect_param2;

    @Override
    public int multiconnect_getParam1() {
        return multiconnect_param1;
    }

    @Override
    public void multiconnect_setParam1(int param1) {
        this.multiconnect_param1 = param1;
    }

    @Override
    public int multiconnect_getParam2() {
        return multiconnect_param2;
    }

    @Override
    public void multiconnect_setParam2(int param2) {
        this.multiconnect_param2 = param2;
    }
}
