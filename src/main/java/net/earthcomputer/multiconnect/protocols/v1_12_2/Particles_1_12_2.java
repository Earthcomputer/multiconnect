package net.earthcomputer.multiconnect.protocols.v1_12_2;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.earthcomputer.multiconnect.impl.IParticleManager;
import net.earthcomputer.multiconnect.impl.ISimpleRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.*;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import java.lang.reflect.Constructor;

import static net.minecraft.particle.ParticleTypes.*;

public class Particles_1_12_2 {

    public static final DefaultParticleType DEPTH_SUSPEND = new MyParticleType(false);
    public static final DefaultParticleType FOOTSTEP = new MyParticleType(false);
    public static final DefaultParticleType SNOW_SHOVEL = new MyParticleType(false);
    public static final ParticleType<BlockStateParticleEffect> BLOCK_DUST = new MyBlockStateParticleType(false);
    public static final DefaultParticleType TAKE = new MyParticleType(false);

    private static void register(ISimpleRegistry<ParticleType<?>> registry, ParticleType<?> particle, int id, String name) {
        registry.register(particle, id, new Identifier(name), false);
    }

    public static void registerParticles(ISimpleRegistry<ParticleType<?>> registry) {
        registry.clear(false);
        register(registry, POOF, 0, "explode");
        register(registry, EXPLOSION, 1, "largeexplode");
        register(registry, EXPLOSION_EMITTER, 2, "hugeexplode");
        register(registry, FIREWORK, 3, "fireworksSpark");
        register(registry, BUBBLE, 4, "bubble");
        register(registry, SPLASH, 5, "splash");
        register(registry, FISHING, 6, "wake");
        register(registry, UNDERWATER, 7, "suspended");
        register(registry, DEPTH_SUSPEND, 8, "depthsuspend");
        register(registry, CRIT, 9, "crit");
        register(registry, ENCHANTED_HIT, 10, "magicCrit");
        register(registry, SMOKE, 11, "smoke");
        register(registry, LARGE_SMOKE, 12, "largesmoke");
        register(registry, EFFECT, 13, "spell");
        register(registry, INSTANT_EFFECT, 14, "instantSpell");
        register(registry, ENTITY_EFFECT, 15, "mobSpell");
        register(registry, AMBIENT_ENTITY_EFFECT, 16, "mobSpellAmbient");
        register(registry, WITCH, 17, "witchMagic");
        register(registry, DRIPPING_WATER, 18, "dripWater");
        register(registry, DRIPPING_LAVA, 19, "dripLava");
        register(registry, ANGRY_VILLAGER, 20, "angryVillager");
        register(registry, HAPPY_VILLAGER, 21, "happyVillager");
        register(registry, MYCELIUM, 22, "townaura");
        register(registry, NOTE, 23, "note");
        register(registry, PORTAL, 24, "portal");
        register(registry, ENCHANT, 25, "enchantmenttable");
        register(registry, FLAME, 26, "flame");
        register(registry, LAVA, 27, "lava");
        register(registry, FOOTSTEP, 28, "footstep");
        register(registry, CLOUD, 29, "cloud");
        register(registry, DUST, 30, "reddust"); // TODO: colors
        register(registry, ITEM_SNOWBALL, 31, "snowballpoof");
        register(registry, SNOW_SHOVEL, 32, "snowshovel");
        register(registry, ITEM_SLIME, 33, "slime");
        register(registry, HEART, 34, "heart");
        register(registry, BARRIER, 35, "barrier");
        register(registry, ITEM, 36, "iconcrack");
        register(registry, BLOCK, 37, "blockcrack");
        register(registry, BLOCK_DUST, 38, "blockdust");
        register(registry, RAIN, 39, "droplet");
        register(registry, TAKE, 40, "take"); // this particle was added in 1.8 and never had any factory
        register(registry, ELDER_GUARDIAN, 41, "mobappearance");
        register(registry, DRAGON_BREATH, 42, "dragonbreath");
        register(registry, END_ROD, 43, "endRod");
        register(registry, DAMAGE_INDICATOR, 44, "damageIndicator");
        register(registry, SWEEP_ATTACK, 45, "sweepAttack");
        register(registry, FALLING_DUST, 46, "fallingdust");
        register(registry, TOTEM_OF_UNDYING, 47, "totem");
        register(registry, SPIT, 48, "spit");
    }

    public static void registerParticleFactories(IParticleManager particleManager) {
        particleManager.multiconnect_registerSpriteAwareFactory(DEPTH_SUSPEND, DepthSuspendFactory::new);
        particleManager.multiconnect_registerFactory(FOOTSTEP, new FootprintParticle.Factory());
        particleManager.multiconnect_registerFactory(SNOW_SHOVEL, new SnowShovelFactory());
        particleManager.multiconnect_registerFactory(BLOCK_DUST, new OldBlockDustParticle.Factory());
    }

    private static class DepthSuspendFactory implements ParticleFactory<DefaultParticleType> {
        private SpriteProvider sprite;
        public DepthSuspendFactory(SpriteProvider sprite) {
            this.sprite = sprite;
        }

        @Override
        public Particle createParticle(DefaultParticleType type, World world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            SuspendParticle particle;
            try {
                Constructor<SuspendParticle> ctor = SuspendParticle.class.getDeclaredConstructor(World.class, double.class, double.class, double.class, double.class, double.class, double.class);
                ctor.setAccessible(true);
                particle = ctor.newInstance(world, x, y, z, xSpeed, ySpeed, zSpeed);
            } catch (ReflectiveOperationException e) {
                throw new AssertionError(e);
            }
            particle.setSprite(sprite);
            return particle;
        }
    }

    private static class SnowShovelFactory implements ParticleFactory<DefaultParticleType> {
        @Override
        public Particle createParticle(DefaultParticleType type, World world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            try {
                Constructor<CrackParticle> ctor = CrackParticle.class.getDeclaredConstructor(World.class, double.class, double.class, double.class, double.class, double.class, double.class, ItemStack.class);
                ctor.setAccessible(true);
                return ctor.newInstance(world, x, y, z, xSpeed, ySpeed, zSpeed, new ItemStack(Blocks.SNOW_BLOCK));
            } catch (ReflectiveOperationException e) {
                throw new AssertionError(e);
            }
        }
    }

    public static class FootprintParticle extends Particle {
        private static final Identifier FOOTPRINT_TEXTURE = new Identifier("textures/particle/footprint.png");
        private int ticks;
        private final int maxTicks = 200;
        private final TextureManager textureManager;

        private FootprintParticle(TextureManager textureManager, World world, double x, double y, double z) {
            super(world, x, y, z, 0, 0, 0);
            this.textureManager = textureManager;
            this.velocityX = 0;
            this.velocityY = 0;
            this.velocityZ = 0;
        }

        @Override
        public void buildGeometry(VertexConsumer vc, Camera camera, float delta) {
            if (!(vc instanceof BufferBuilder)) return;

            float alpha = (ticks + delta) / maxTicks;
            alpha *= alpha;
            alpha = 2 - (alpha * 2);
            if (alpha > 1)
                alpha = 1;
            alpha *= 0.2;

            RenderSystem.disableLighting();
            final float radius = 0.125f;
            Vec3d cameraPos = camera.getPos();
            float x = (float) (this.x - cameraPos.getX());
            float y = (float) (this.y - cameraPos.getY());
            float z = (float) (this.z - cameraPos.getZ());
            float light = world.getBrightness(new BlockPos(this.x, this.y, this.z));
            textureManager.bindTexture(FOOTPRINT_TEXTURE);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
            ((BufferBuilder) vc).begin(GL11.GL_QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            vc.vertex(x - radius, y, z + radius).texture(0, 1).color(light, light, light, alpha).next();
            vc.vertex(x + radius, y, z + radius).texture(1, 1).color(light, light, light, alpha).next();
            vc.vertex(x + radius, y, z - radius).texture(1, 0).color(light, light, light, alpha).next();
            vc.vertex(x - radius, y, z - radius).texture(0, 0).color(light, light, light, alpha).next();
            Tessellator.getInstance().draw();
            RenderSystem.disableBlend();
            RenderSystem.enableLighting();
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
            public Particle createParticle(DefaultParticleType type, World world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
                return new FootprintParticle(MinecraftClient.getInstance().getTextureManager(), world, x, y, z);
            }
        }
    }

    private static class OldBlockDustParticle extends BlockDustParticle {

        public OldBlockDustParticle(World world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, BlockState state) {
            super(world, x, y, z, xSpeed, ySpeed, zSpeed, state);
            velocityX = xSpeed;
            velocityY = ySpeed;
            velocityZ = zSpeed;
        }

        public static class Factory implements ParticleFactory<BlockStateParticleEffect> {

            @Override
            public Particle createParticle(BlockStateParticleEffect type, World world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
                return new OldBlockDustParticle(world, x, y, z, xSpeed, ySpeed, zSpeed, type.getBlockState());
            }
        }
    }

}
