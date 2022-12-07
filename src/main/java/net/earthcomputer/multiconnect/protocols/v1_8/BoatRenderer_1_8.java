package net.earthcomputer.multiconnect.protocols.v1_8;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.Boat;
import org.joml.Quaternionf;

public class BoatRenderer_1_8 extends EntityRenderer<Boat> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("multiconnect", "textures/entity/boat_1_8.png");
    private final BoatModel_1_8 model;

    public BoatRenderer_1_8(EntityRendererProvider.Context ctx) {
        super(ctx);
        shadowRadius = 0.8f;
        model = new BoatModel_1_8(ctx.bakeLayer(BoatModel_1_8.MODEL_LAYER));
    }

    @Override
    public ResourceLocation getTextureLocation(Boat entity) {
        return TEXTURE;
    }

    @Override
    public void render(Boat entity, float yaw, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light) {
        matrices.pushPose();
        matrices.translate(0, 0.375, 0);
        matrices.mulPose(new Quaternionf().rotateY((180 - yaw) * Mth.DEG_TO_RAD));
        float damageWobbleTicks = entity.getHurtTime() - tickDelta;
        float damageWobbleStrength = entity.getDamage() - tickDelta;
        if (damageWobbleStrength < 0) {
            damageWobbleStrength = 0;
        }

        if (damageWobbleTicks > 0) {
            matrices.mulPose(new Quaternionf().rotateX((Mth.sin(damageWobbleTicks) * damageWobbleTicks * damageWobbleStrength / 10 * entity.getHurtDir()) * Mth.DEG_TO_RAD));
        }

        matrices.scale(-1, -1, 1);
        model.setupAnim(entity, tickDelta, 0, -0.1f, 0, 0);
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(model.renderType(TEXTURE));
        model.renderToBuffer(matrices, vertexConsumer, light, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);

        matrices.popPose();
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }
}
