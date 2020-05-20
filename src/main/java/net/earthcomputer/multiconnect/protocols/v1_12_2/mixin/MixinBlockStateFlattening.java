package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import com.mojang.serialization.Dynamic;
import net.earthcomputer.multiconnect.protocols.v1_12_2.BlockStateReverseFlattening;
import net.minecraft.datafixer.fix.BlockStateFlattening;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mixin(BlockStateFlattening.class)
public abstract class MixinBlockStateFlattening {

    @Inject(method = "putStates", at = @At("RETURN"))
    private static void onPutStates(int id, String newState, String[] oldStates, CallbackInfo ci) {
        Dynamic<?> oldState = BlockStateFlattening.parseState(oldStates.length == 0 ? newState : oldStates[0]);
        BlockStateReverseFlattening.IDS_TO_OLD_STATES[id] = oldState;
        for (String old : oldStates) {
            handleOldState(old);
        }
    }

    @Unique
    private static void handleOldState(String old) {
        Dynamic<?> oldState = BlockStateFlattening.parseState(old);
        Identifier id = new Identifier(oldState.get("Name").asString(""));
        Map<String, String> properties = oldState.get("Properties").asMap(k -> k.asString(""), v -> v.asString(""));
        BlockStateReverseFlattening.OLD_PROPERTIES.computeIfAbsent(id, k -> properties.keySet().stream().sorted().collect(Collectors.toList()));
        properties.forEach((name, value) -> {
            List<String> values = BlockStateReverseFlattening.OLD_PROPERTY_VALUES.computeIfAbsent(Pair.of(id, name), k -> new ArrayList<>());
            if (!values.contains(value))
                values.add(value);
        });
    }

    @Inject(method = "fillEmptyStates", at = @At("RETURN"))
    private static void onFillEmptyStates(CallbackInfo ci) {
        for (int i = 0; i < BlockStateReverseFlattening.IDS_TO_OLD_STATES.length; i++) {
            if (BlockStateReverseFlattening.IDS_TO_OLD_STATES[i] == null)
                BlockStateReverseFlattening.IDS_TO_OLD_STATES[i] = BlockStateReverseFlattening.IDS_TO_OLD_STATES[i >> 4 << 4];
        }
    }

}
