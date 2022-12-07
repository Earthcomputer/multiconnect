package net.earthcomputer.multiconnect.protocols.v1_12;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.earthcomputer.multiconnect.debug.DebugUtils;
import net.earthcomputer.multiconnect.protocols.generic.IParticleManager;
import net.earthcomputer.multiconnect.protocols.generic.MyParticleType;
import net.earthcomputer.multiconnect.protocols.v1_12.mixin.BreakingItemParticleAccessor;
import net.earthcomputer.multiconnect.protocols.v1_12.mixin.SuspendTownParticleAccessor;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class Particles_1_12_2 {

    public static final SimpleParticleType DEPTH_SUSPEND = new MyParticleType(false);
    public static final SimpleParticleType FOOTSTEP = new MyParticleType(false);
    public static final SimpleParticleType SNOW_SHOVEL = new MyParticleType(false);
    public static final ParticleType<BlockParticleOption> BLOCK_DUST = new MyBlockStateParticleType(false);
    public static final SimpleParticleType TAKE = new MyParticleType(false);

    public static void register() {
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, "multiconnect:depth_suspend", DEPTH_SUSPEND);
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, "multiconnect:footstep", FOOTSTEP);
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, "multiconnect:snow_shovel", SNOW_SHOVEL);
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, "multiconnect:block_dust", BLOCK_DUST);
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, "multiconnect:take", TAKE);
    }

    public static void registerFactories() {
        if (!DebugUtils.UNIT_TEST_MODE) {
            IParticleManager particleManager = (IParticleManager) Minecraft.getInstance().particleEngine;
            particleManager.multiconnect_registerSpriteSet(DEPTH_SUSPEND, DepthSuspendFactory::new);
            particleManager.multiconnect_registerProvider(FOOTSTEP, new FootprintParticle.Factory());
            particleManager.multiconnect_registerProvider(SNOW_SHOVEL, new SnowShovelFactory());
            particleManager.multiconnect_registerProvider(BLOCK_DUST, new OldBlockDustParticle.Factory());
        }
    }

    private static class DepthSuspendFactory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;
        public DepthSuspendFactory(SpriteSet sprite) {
            this.sprite = sprite;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            SuspendedTownParticle particle = SuspendTownParticleAccessor.constructor(level, x, y, z, xSpeed, ySpeed, zSpeed);
            particle.pickSprite(sprite);
            return particle;
        }
    }

    private static class SnowShovelFactory implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return BreakingItemParticleAccessor.constructor(level, x, y, z, xSpeed, ySpeed, zSpeed, new ItemStack(Blocks.SNOW_BLOCK));
        }
    }

    public static class FootprintParticle extends Particle {
        private static final ResourceLocation FOOTPRINT_TEXTURE = new ResourceLocation("textures/particle/footprint.png");
        private int ticks;
        private final int maxTicks = 200;

        private FootprintParticle(ClientLevel level, double x, double y, double z) {
            super(level, x, y, z, 0, 0, 0);
            this.xd = 0;
            this.yd = 0;
            this.zd = 0;
        }

        @SuppressWarnings("deprecation")
        @Override
        public void render(VertexConsumer vc, Camera camera, float delta) {
            if (!(vc instanceof BufferBuilder bufferBuilder)) return;

            float alpha = (ticks + delta) / maxTicks;
            alpha *= alpha;
            alpha = 2 - (alpha * 2);
            if (alpha > 1)
                alpha = 1;
            alpha *= 0.2;

            final float radius = 0.125f;
            Vec3 cameraPos = camera.getPosition();
            float x = (float) (this.x - cameraPos.x());
            float y = (float) (this.y - cameraPos.y());
            float z = (float) (this.z - cameraPos.z());
            float light = level.getLightLevelDependentMagicValue(new BlockPos(this.x, this.y, this.z));
            int oldTextureId = RenderSystem.getShaderTexture(0);
            RenderSystem.setShaderTexture(0, FOOTPRINT_TEXTURE);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            vc.vertex(x - radius, y, z + radius).uv(0, 1).color(light, light, light, alpha).endVertex();
            vc.vertex(x + radius, y, z + radius).uv(1, 1).color(light, light, light, alpha).endVertex();
            vc.vertex(x + radius, y, z - radius).uv(1, 0).color(light, light, light, alpha).endVertex();
            vc.vertex(x - radius, y, z - radius).uv(0, 0).color(light, light, light, alpha).endVertex();
            Tesselator.getInstance().end();
            RenderSystem.disableBlend();
            RenderSystem.setShaderTexture(0, oldTextureId);
        }

        @Override
        public void tick() {
            ticks++;
            if (ticks == maxTicks)
                remove();
        }

        @Override
        public ParticleRenderType getRenderType() {
            return ParticleRenderType.CUSTOM;
        }

        public static class Factory implements ParticleProvider<SimpleParticleType> {
            @Override
            public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
                return new FootprintParticle(level, x, y, z);
            }
        }
    }

    private static class OldBlockDustParticle extends TerrainParticle {

        public OldBlockDustParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, BlockState state) {
            super(level, x, y, z, xSpeed, ySpeed, zSpeed, state);
            xd = xSpeed;
            yd = ySpeed;
            zd = zSpeed;
        }

        public static class Factory implements ParticleProvider<BlockParticleOption> {

            @Override
            public Particle createParticle(BlockParticleOption type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
                return new OldBlockDustParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, type.getState());
            }
        }
    }

}
