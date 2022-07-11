package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import com.google.common.collect.ImmutableMap;
import net.earthcomputer.multiconnect.protocols.v1_8.BoatModel_1_8;
import net.minecraft.client.model.geom.LayerDefinitions;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LayerDefinitions.class)
public class LayerDefinitionsMixin {
    @ModifyVariable(method = "createRoots", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
    private static ImmutableMap.Builder<ModelLayerLocation, LayerDefinition> addBoatModel(ImmutableMap.Builder<ModelLayerLocation, LayerDefinition> builder) {
        return builder.put(BoatModel_1_8.MODEL_LAYER, BoatModel_1_8.getTexturedModelData());
    }
}
