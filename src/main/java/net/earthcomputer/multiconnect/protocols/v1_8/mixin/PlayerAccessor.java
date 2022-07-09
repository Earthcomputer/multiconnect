package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Player.class)
public interface PlayerAccessor {
    @Accessor("DATA_PLAYER_ABSORPTION_ID")
    static EntityDataAccessor<Float> getDataPlayerAbsorptionId() {
        return MixinHelper.fakeInstance();
    }

    @Accessor("DATA_PLAYER_MODE_CUSTOMISATION")
    static EntityDataAccessor<Byte> getDataPlayerModeCustomisation() {
        return MixinHelper.fakeInstance();
    }
}
