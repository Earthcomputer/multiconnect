package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.monster.Slime;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Slime.class)
public interface SlimeAccessor {
    @Accessor("ID_SIZE")
    static EntityDataAccessor<Integer> getIdSize() {
        return MixinHelper.fakeInstance();
    }
}
