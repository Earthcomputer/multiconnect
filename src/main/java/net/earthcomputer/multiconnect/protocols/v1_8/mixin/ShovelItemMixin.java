package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.world.item.ShovelItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

import java.util.Map;

@Mixin(ShovelItem.class)
public class ShovelItemMixin {
    @Redirect(method = "useOn",
            at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;", ordinal = 0),
            slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/world/item/ShovelItem;FLATTENABLES:Ljava/util/Map;")))
    private Object redirectGetFlattenable(Map<Object, Object> map, Object grassBlock) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_8) {
            return null;
        } else {
            return map.get(grassBlock);
        }
    }
}
