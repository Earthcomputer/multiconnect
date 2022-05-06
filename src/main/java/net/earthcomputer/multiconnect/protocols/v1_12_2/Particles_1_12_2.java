package net.earthcomputer.multiconnect.protocols.v1_12_2;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.earthcomputer.multiconnect.protocols.generic.IParticleManager;
import net.earthcomputer.multiconnect.protocols.v1_12_2.mixin.CrackParticleAccessor;
import net.earthcomputer.multiconnect.protocols.v1_12_2.mixin.SuspendParticleAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.particle.*;
import net.minecraft.client.render.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class Particles_1_12_2 {

    public static final DefaultParticleType DEPTH_SUSPEND = new MyParticleType(false);
    public static final DefaultParticleType FOOTSTEP = new MyParticleType(false);
    public static final DefaultParticleType SNOW_SHOVEL = new MyParticleType(false);
    public static final ParticleType<BlockStateParticleEffect> BLOCK_DUST = new MyBlockStateParticleType(false);
    public static final DefaultParticleType TAKE = new MyParticleType(false);

    public static void registerParticleFactories(IParticleManager particleManager) {
        particleManager.multiconnect_registerSpriteAwareFactory(DEPTH_SUSPEND, DepthSuspendFactory::new);
        particleManager.multiconnect_registerFactory(FOOTSTEP, new FootprintParticle.Factory());
        particleManager.multiconnect_registerFactory(SNOW_SHOVEL, new SnowShovelFactory());
        particleManager.multiconnect_registerFactory(BLOCK_DUST, new OldBlockDustParticle.Factory());
    }

    private static class DepthSuspendFactory implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider sprite;
        public DepthSuspendFactory(SpriteProvider sprite) {
            this.sprite = sprite;
        }

        @Override
        public Particle createParticle(DefaultParticleType type, ClientWorld world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            SuspendParticle particle = SuspendParticleAccessor.constructor(world, x, y, z, xSpeed, ySpeed, zSpeed);
            particle.setSprite(sprite);
            return particle;
        }
    }

    private static class SnowShovelFactory implements ParticleFactory<DefaultParticleType> {
        @Override
        public Particle createParticle(DefaultParticleType type, ClientWorld world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return CrackParticleAccessor.constructor(world, x, y, z, xSpeed, ySpeed, zSpeed, new ItemStack(Blocks.SNOW_BLOCK));
        }
    }

    public static class FootprintParticle extends Particle {
        private static final Identifier FOOTPRINT_TEXTURE = new Identifier("textures/particle/footprint.png");
        private int ticks;
        private final int maxTicks = 200;

        private FootprintParticle(ClientWorld world, double x, double y, double z) {
            super(world, x, y, z, 0, 0, 0);
            this.velocityX = 0;
            this.velocityY = 0;
            this.velocityZ = 0;
        }

        @SuppressWarnings("deprecation")
        @Override
        public void buildGeometry(VertexConsumer vc, Camera camera, float delta) {
            if (!(vc instanceof BufferBuilder)) return;

            float alpha = (ticks + delta) / maxTicks;
            alpha *= alpha;
            alpha = 2 - (alpha * 2);
            if (alpha > 1)
                alpha = 1;
            alpha *= 0.2;

            final float radius = 0.125f;
            Vec3d cameraPos = camera.getPos();
            float x = (float) (this.x - cameraPos.getX());
            float y = (float) (this.y - cameraPos.getY());
            float z = (float) (this.z - cameraPos.getZ());
            float light = world.getBrightness(new BlockPos(this.x, this.y, this.z));
            int oldTextureId = RenderSystem.getShaderTexture(0);
            RenderSystem.setShaderTexture(0, FOOTPRINT_TEXTURE);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
            ((BufferBuilder) vc).begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            vc.vertex(x - radius, y, z + radius).texture(0, 1).color(light, light, light, alpha).next();
            vc.vertex(x + radius, y, z + radius).texture(1, 1).color(light, light, light, alpha).next();
            vc.vertex(x + radius, y, z - radius).texture(1, 0).color(light, light, light, alpha).next();
            vc.vertex(x - radius, y, z - radius).texture(0, 0).color(light, light, light, alpha).next();
            Tessellator.getInstance().draw();
            RenderSystem.disableBlend();
            RenderSystem.setShaderTexture(0, oldTextureId);
        }

        @Override
        public void tick() {
            ticks++;
            if (ticks == maxTicks)
                markDead();
        }

        @Override
        public ParticleTextureSheet getType() {
            return ParticleTextureSheet.CUSTOM;
        }

        public static class Factory implements ParticleFactory<DefaultParticleType> {
            @Override
            public Particle createParticle(DefaultParticleType type, ClientWorld world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
                return new FootprintParticle(world, x, y, z);
            }
        }
    }

    private static class OldBlockDustParticle extends BlockDustParticle {

        public OldBlockDustParticle(ClientWorld world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, BlockState state) {
            super(world, x, y, z, xSpeed, ySpeed, zSpeed, state);
            velocityX = xSpeed;
            velocityY = ySpeed;
            velocityZ = zSpeed;
        }

        public static class Factory implements ParticleFactory<BlockStateParticleEffect> {

            @Override
            public Particle createParticle(BlockStateParticleEffect type, ClientWorld world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
                return new OldBlockDustParticle(world, x, y, z, xSpeed, ySpeed, zSpeed, type.getBlockState());
            }
        }
    }

}
