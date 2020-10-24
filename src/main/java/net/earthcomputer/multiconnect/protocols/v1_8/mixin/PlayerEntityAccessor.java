package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerEntity.class)
public interface PlayerEntityAccessor {
    @Accessor("ABSORPTION_AMOUNT")
    static TrackedData<Float> getAbsorptionAmount() {
        return MixinHelper.fakeInstance();
    }

    @Accessor("PLAYER_MODEL_PARTS")
    static TrackedData<Byte> getPlayerModelParts() {
        return MixinHelper.fakeInstance();
    }
}
