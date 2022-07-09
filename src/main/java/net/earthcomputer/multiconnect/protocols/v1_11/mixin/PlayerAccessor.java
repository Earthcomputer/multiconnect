package net.earthcomputer.multiconnect.protocols.v1_11.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Player.class)
public interface PlayerAccessor {
    @Accessor("DATA_SHOULDER_LEFT")
    static EntityDataAccessor<CompoundTag> getDataShoulderLeft() {
        return MixinHelper.fakeInstance();
    }

    @Accessor("DATA_SHOULDER_RIGHT")
    static EntityDataAccessor<CompoundTag> getDataShoulderRight() {
        return MixinHelper.fakeInstance();
    }
}
