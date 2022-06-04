package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StructureBlockBlockEntity.class)
public class MixinStructureBlockBlockEntity {
    @Unique
    private String name_1122;

    @Inject(method = "getTemplateName", at = @At("HEAD"), cancellable = true)
    private void onGetStructureName(CallbackInfoReturnable<String> ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2) {
            ci.setReturnValue(name_1122 == null ? "" : name_1122);
        }
    }

    @Inject(method = "hasStructureName", at = @At("RETURN"), cancellable = true)
    private void onHasStructureName(CallbackInfoReturnable<Boolean> ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2 && name_1122 != null) {
            ci.setReturnValue(true);
        }
    }

    @Inject(method = "setTemplateName(Ljava/lang/String;)V", at = @At("HEAD"))
    private void onSetStructureName(String name, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2) {
            name_1122 = name;
        }
    }
}
