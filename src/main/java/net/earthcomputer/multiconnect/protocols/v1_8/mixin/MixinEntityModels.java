package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import com.google.common.collect.ImmutableMap;
import net.earthcomputer.multiconnect.protocols.v1_8.BoatModel_1_8;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModels;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EntityModels.class)
public class MixinEntityModels {
    @ModifyVariable(method = "getModels", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
    private static ImmutableMap.Builder<EntityModelLayer, TexturedModelData> addBoatModel(ImmutableMap.Builder<EntityModelLayer, TexturedModelData> builder) {
        return builder.put(BoatModel_1_8.MODEL_LAYER, BoatModel_1_8.getTexturedModelData());
    }
}
