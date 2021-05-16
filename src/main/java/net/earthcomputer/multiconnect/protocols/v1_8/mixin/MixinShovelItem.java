package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.item.ShovelItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

import java.util.Map;

@Mixin(ShovelItem.class)
public class MixinShovelItem {
    @Redirect(method = "useOnBlock",
            at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;", ordinal = 0),
            slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/item/ShovelItem;PATH_STATES:Ljava/util/Map;")))
    private Object redirectGetPathBlock(Map<Object, Object> map, Object grassBlock) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_8) {
            return null;
        } else {
            return map.get(grassBlock);
        }
    }
}
