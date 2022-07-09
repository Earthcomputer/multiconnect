package net.earthcomputer.multiconnect.protocols.v1_12.mixin;

import com.mojang.serialization.Dynamic;
import net.earthcomputer.multiconnect.protocols.v1_12.BlockStateReverseData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.fixes.BlockStateData;
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

@Mixin(BlockStateData.class)
public abstract class BlockStateDataMixin {

    @Inject(method = "register", at = @At("RETURN"))
    private static void onPutStates(int id, String newState, String[] oldStates, CallbackInfo ci) {
        for (String old : oldStates) {
            multiconnect_handleOldState(old);
        }
    }

    @Unique
    private static void multiconnect_handleOldState(String old) {
        Dynamic<?> oldState = BlockStateData.parse(old);
        ResourceLocation id = new ResourceLocation(oldState.get("Name").asString(""));
        Map<String, String> properties = oldState.get("Properties").asMap(k -> k.asString(""), v -> v.asString(""));
        BlockStateReverseData.OLD_PROPERTIES.computeIfAbsent(id, k -> properties.keySet().stream().sorted().collect(Collectors.toList()));
        properties.forEach((name, value) -> {
            List<String> values = BlockStateReverseData.OLD_PROPERTY_VALUES.computeIfAbsent(Pair.of(id, name), k -> new ArrayList<>());
            if (!values.contains(value))
                values.add(value);
        });
    }
}
