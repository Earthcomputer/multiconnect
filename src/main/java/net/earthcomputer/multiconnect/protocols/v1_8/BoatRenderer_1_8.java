package net.earthcomputer.multiconnect.protocols.v1_8;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;

public class BoatRenderer_1_8 extends EntityRenderer<BoatEntity> {
    private static final Identifier TEXTURE = new Identifier("multiconnect", "textures/entity/boat_1_8.png");
    private final BoatModel_1_8 model;

    public BoatRenderer_1_8(EntityRendererFactory.Context ctx) {
        super(ctx);
        shadowRadius = 0.8f;
        model = new BoatModel_1_8(ctx.getPart(BoatModel_1_8.MODEL_LAYER));
    }

    @Override
    public Identifier getTexture(BoatEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(BoatEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();
        matrices.translate(0, 0.375, 0);
        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180 - yaw));
        float damageWobbleTicks = entity.getDamageWobbleTicks() - tickDelta;
        float damageWobbleStrength = entity.getDamageWobbleStrength() - tickDelta;
        if (damageWobbleStrength < 0) {
            damageWobbleStrength = 0;
        }

        if (damageWobbleTicks > 0) {
            matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(MathHelper.sin(damageWobbleTicks) * damageWobbleTicks * damageWobbleStrength / 10 * entity.getDamageWobbleSide()));
        }

        matrices.scale(-1, -1, 1);
        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(90));
        model.setAngles(entity, tickDelta, 0, -0.1f, 0, 0);
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(model.getLayer(TEXTURE));
        model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1, 1, 1, 1);

        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }
}
