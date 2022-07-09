package net.earthcomputer.multiconnect.protocols.v1_8;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.vehicle.Boat;

public class BoatModel_1_8 extends ListModel<Boat> {
    public static final ModelLayerLocation MODEL_LAYER = new ModelLayerLocation(new ResourceLocation("multiconnect", "boat_1_8"), "main");
    private final ImmutableList<ModelPart> parts;

    public BoatModel_1_8(ModelPart root) {
        this.parts = ImmutableList.of(root.getChild("bottom"), root.getChild("back"), root.getChild("front"), root.getChild("right"), root.getChild("left"));
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition root = modelData.getRoot();
        final float width = 24;
        final float wallHeight = 6;
        final float baseWidth = 20;
        final float pivotY = 4;
        root.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(0, 8).addBox(-width/2, -baseWidth/2+2, -3, width, baseWidth-4, 4), PartPose.offsetAndRotation(0, pivotY, 0, (float)Math.PI / 2, 0, 0));
        root.addOrReplaceChild("back", CubeListBuilder.create().texOffs(0, 0).addBox(-width/2+2, -wallHeight-1, -1, width-4, wallHeight, 2), PartPose.offsetAndRotation(-width/2+1, pivotY, 0, 0, (float)Math.PI * 1.5f, 0));
        root.addOrReplaceChild("front", CubeListBuilder.create().texOffs(0, 0).addBox(-width/2+2, -wallHeight-1, -1, width-4, wallHeight, 2), PartPose.offsetAndRotation(width/2-1, pivotY, 0, 0, (float)Math.PI / 2, 0));
        root.addOrReplaceChild("right", CubeListBuilder.create().texOffs(0, 0).addBox(-width/2+2, -wallHeight-1, -1, width-4, wallHeight, 2), PartPose.offsetAndRotation(0, pivotY, -baseWidth/2+1, 0, (float)Math.PI, 0));
        root.addOrReplaceChild("left", CubeListBuilder.create().texOffs(0, 0).addBox(-width/2+2, -wallHeight-1, -1, width-4, wallHeight, 2), PartPose.offset(0, pivotY, baseWidth/2-1));
        return LayerDefinition.create(modelData, 64, 32);
    }

    @Override
    public Iterable<ModelPart> parts() {
        return parts;
    }

    @Override
    public void setupAnim(Boat entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }
}
