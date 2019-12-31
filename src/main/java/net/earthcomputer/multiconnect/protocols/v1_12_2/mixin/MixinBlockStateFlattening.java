package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import com.mojang.datafixers.Dynamic;
import net.earthcomputer.multiconnect.protocols.v1_12_2.BlockStateReverseFlattening;
import net.minecraft.datafixer.fix.BlockStateFlattening;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockStateFlattening.class)
public abstract class MixinBlockStateFlattening {

    @Inject(method = "putStates", at = @At("RETURN"))
    private static void onPutStates(int id, String newState, String[] oldStates, CallbackInfo ci) {
        Dynamic<?> oldState = BlockStateFlattening.parseState(oldStates.length == 0 ? newState : oldStates[0]);
        BlockStateReverseFlattening.IDS_TO_OLD_STATES[id] = oldState;
    }

    @Inject(method = "fillEmptyStates", at = @At("RETURN"))
    private static void onFillEmptyStates(CallbackInfo ci) {
        for (int i = 0; i < BlockStateReverseFlattening.IDS_TO_OLD_STATES.length; i++) {
            if (BlockStateReverseFlattening.IDS_TO_OLD_STATES[i] == null)
                BlockStateReverseFlattening.IDS_TO_OLD_STATES[i] = BlockStateReverseFlattening.IDS_TO_OLD_STATES[i >> 4 << 4];
        }
    }

}
