package net.earthcomputer.multiconnect.protocols.v1_12_2;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.earthcomputer.multiconnect.impl.IParticleManager;
import net.earthcomputer.multiconnect.impl.ISimpleRegistry;
import net.earthcomputer.multiconnect.protocols.v1_12_2.mixin.CrackParticleAccessor;
import net.earthcomputer.multiconnect.protocols.v1_12_2.mixin.SuspendParticleAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import static net.minecraft.particles.ParticleTypes.*;

public class Particles_1_12_2 {

    private static BiMap<ParticleType<?>, String> OLD_NAMES = HashBiMap.create();

    public static final BasicParticleType DEPTH_SUSPEND = new MyParticleType(false);
    public static final BasicParticleType FOOTSTEP = new MyParticleType(false);
    public static final BasicParticleType SNOW_SHOVEL = new MyParticleType(false);
    public static final ParticleType<BlockParticleData> BLOCK_DUST = new MyBlockStateParticleType(false);
    public static final BasicParticleType TAKE = new MyParticleType(false);

    static {
        OLD_NAMES.put(POOF, "explode");
        OLD_NAMES.put(EXPLOSION, "largeexplode");
        OLD_NAMES.put(EXPLOSION_EMITTER, "hugeexplode");
        OLD_NAMES.put(FIREWORK, "fireworksSpark");
        OLD_NAMES.put(BUBBLE, "bubble");
        OLD_NAMES.put(SPLASH, "splash");
        OLD_NAMES.put(FISHING, "wake");
        OLD_NAMES.put(UNDERWATER, "suspended");
        OLD_NAMES.put(DEPTH_SUSPEND, "depthsuspend");
        OLD_NAMES.put(CRIT, "crit");
        OLD_NAMES.put(ENCHANTED_HIT, "magicCrit");
        OLD_NAMES.put(SMOKE, "smoke");
        OLD_NAMES.put(LARGE_SMOKE, "largesmoke");
        OLD_NAMES.put(EFFECT, "spell");
        OLD_NAMES.put(INSTANT_EFFECT, "instantSpell");
        OLD_NAMES.put(ENTITY_EFFECT, "mobSpell");
        OLD_NAMES.put(AMBIENT_ENTITY_EFFECT, "mobSpellAmbient");
        OLD_NAMES.put(WITCH, "witchMagic");
        OLD_NAMES.put(DRIPPING_WATER, "dripWater");
        OLD_NAMES.put(DRIPPING_LAVA, "dripLava");
        OLD_NAMES.put(ANGRY_VILLAGER, "angryVillager");
        OLD_NAMES.put(HAPPY_VILLAGER, "happyVillager");
        OLD_NAMES.put(MYCELIUM, "townaura");
        OLD_NAMES.put(NOTE, "note");
        OLD_NAMES.put(PORTAL, "portal");
        OLD_NAMES.put(ENCHANT, "enchantmenttable");
        OLD_NAMES.put(FLAME, "flame");
        OLD_NAMES.put(LAVA, "lava");
        OLD_NAMES.put(FOOTSTEP, "footstep");
        OLD_NAMES.put(CLOUD, "cloud");
        OLD_NAMES.put(DUST, "reddust");
        OLD_NAMES.put(ITEM_SNOWBALL, "snowballpoof");
        OLD_NAMES.put(SNOW_SHOVEL, "snowshovel");
        OLD_NAMES.put(ITEM_SLIME, "slime");
        OLD_NAMES.put(HEART, "heart");
        OLD_NAMES.put(BARRIER, "barrier");
        OLD_NAMES.put(ITEM, "iconcrack");
        OLD_NAMES.put(BLOCK, "blockcrack");
        OLD_NAMES.put(BLOCK_DUST, "blockdust");
        OLD_NAMES.put(RAIN, "droplet");
        OLD_NAMES.put(TAKE, "take");
        OLD_NAMES.put(ELDER_GUARDIAN, "mobappearance");
        OLD_NAMES.put(DRAGON_BREATH, "dragonbreath");
        OLD_NAMES.put(END_ROD, "endRod");
        OLD_NAMES.put(DAMAGE_INDICATOR, "damageIndicator");
        OLD_NAMES.put(SWEEP_ATTACK, "sweepAttack");
        OLD_NAMES.put(FALLING_DUST, "fallingdust");
        OLD_NAMES.put(TOTEM_OF_UNDYING, "totem");
        OLD_NAMES.put(SPIT, "spit");
    }

    private static void register(ISimpleRegistry<ParticleType<?>> registry, ParticleType<?> particle, int id, String name) {
        registry.register(particle, id, new ResourceLocation(name), false);
    }

    public static void registerParticles(ISimpleRegistry<ParticleType<?>> registry) {
        registry.clear(false);
        register(registry, POOF, 0, "poof");
        register(registry, EXPLOSION, 1, "explosion");
        register(registry, EXPLOSION_EMITTER, 2, "explosion_emitter");
        register(registry, FIREWORK, 3, "firework");
        register(registry, BUBBLE, 4, "bubble");
        register(registry, SPLASH, 5, "splash");
        register(registry, FISHING, 6, "fishing");
        register(registry, UNDERWATER, 7, "underwater");
        register(registry, DEPTH_SUSPEND, 8, "depth_suspend");
        register(registry, CRIT, 9, "crit");
        register(registry, ENCHANTED_HIT, 10, "enchanted_hit");
        register(registry, SMOKE, 11, "smoke");
        register(registry, LARGE_SMOKE, 12, "large_smoke");
        register(registry, EFFECT, 13, "effect");
        register(registry, INSTANT_EFFECT, 14, "instant_effect");
        register(registry, ENTITY_EFFECT, 15, "entity_effect");
        register(registry, AMBIENT_ENTITY_EFFECT, 16, "ambient_entity_effect");
        register(registry, WITCH, 17, "witch");
        register(registry, DRIPPING_WATER, 18, "dripping_water");
        register(registry, DRIPPING_LAVA, 19, "dripping_lava");
        register(registry, ANGRY_VILLAGER, 20, "angry_villager");
        register(registry, HAPPY_VILLAGER, 21, "happy_villager");
        register(registry, MYCELIUM, 22, "mycelium");
        register(registry, NOTE, 23, "note");
        register(registry, PORTAL, 24, "portal");
        register(registry, ENCHANT, 25, "enchant");
        register(registry, FLAME, 26, "flame");
        register(registry, LAVA, 27, "lava");
        register(registry, FOOTSTEP, 28, "footstep");
        register(registry, CLOUD, 29, "cloud");
        register(registry, DUST, 30, "dust");
        register(registry, ITEM_SNOWBALL, 31, "item_snowball");
        register(registry, SNOW_SHOVEL, 32, "snow_shovel");
        register(registry, ITEM_SLIME, 33, "item_slime");
        register(registry, HEART, 34, "heart");
        register(registry, BARRIER, 35, "barrier");
        register(registry, ITEM, 36, "item");
        register(registry, BLOCK, 37, "block");
        register(registry, BLOCK_DUST, 38, "block_dust");
        register(registry, RAIN, 39, "rain");
        register(registry, TAKE, 40, "take"); // this particle was added in 1.8 and never had any factory
        register(registry, ELDER_GUARDIAN, 41, "elder_guardian");
        register(registry, DRAGON_BREATH, 42, "dragon_breath");
        register(registry, END_ROD, 43, "end_rod");
        register(registry, DAMAGE_INDICATOR, 44, "damage_indicator");
        register(registry, SWEEP_ATTACK, 45, "sweep_attack");
        register(registry, FALLING_DUST, 46, "falling_dust");
        register(registry, TOTEM_OF_UNDYING, 47, "totem_of_undying");
        register(registry, SPIT, 48, "spit");
    }

    public static void registerParticleFactories(IParticleManager particleManager) {
        particleManager.multiconnect_registerSpriteAwareFactory(DEPTH_SUSPEND, DepthSuspendFactory::new);
        particleManager.multiconnect_registerFactory(FOOTSTEP, new FootprintParticle.Factory());
        particleManager.multiconnect_registerFactory(SNOW_SHOVEL, new SnowShovelFactory());
        particleManager.multiconnect_registerFactory(BLOCK_DUST, new OldBlockDustParticle.Factory());
    }

    private static class DepthSuspendFactory implements IParticleFactory<BasicParticleType> {
        private IAnimatedSprite sprite;
        public DepthSuspendFactory(IAnimatedSprite sprite) {
            this.sprite = sprite;
        }

        @Override
        public Particle makeParticle(BasicParticleType type, World world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            SuspendedTownParticle particle = SuspendParticleAccessor.constructor(world, x, y, z, xSpeed, ySpeed, zSpeed);
            particle.selectSpriteRandomly(sprite);
            return particle;
        }
    }

    private static class SnowShovelFactory implements IParticleFactory<BasicParticleType> {
        @Override
        public Particle makeParticle(BasicParticleType type, World world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return CrackParticleAccessor.constructor(world, x, y, z, xSpeed, ySpeed, zSpeed, new ItemStack(Blocks.SNOW_BLOCK));
        }
    }

    public static class FootprintParticle extends Particle {
        private static final ResourceLocation FOOTPRINT_TEXTURE = new ResourceLocation("textures/particle/footprint.png");
        private int ticks;
        private final int maxTicks = 200;
        private final TextureManager textureManager;

        private FootprintParticle(TextureManager textureManager, World world, double x, double y, double z) {
            super(world, x, y, z, 0, 0, 0);
            this.textureManager = textureManager;
            this.motionX = 0;
            this.motionY = 0;
            this.motionZ = 0;
        }

        @SuppressWarnings("deprecation")
        @Override
        public void renderParticle(IVertexBuilder vc, ActiveRenderInfo camera, float delta) {
            if (!(vc instanceof BufferBuilder)) return;

            float alpha = (ticks + delta) / maxTicks;
            alpha *= alpha;
            alpha = 2 - (alpha * 2);
            if (alpha > 1)
                alpha = 1;
            alpha *= 0.2;

            RenderSystem.disableLighting();
            final float radius = 0.125f;
            Vec3d cameraPos = camera.getProjectedView();
            float x = (float) (this.posX - cameraPos.getX());
            float y = (float) (this.posY - cameraPos.getY());
            float z = (float) (this.posZ - cameraPos.getZ());
            float light = world.getBrightness(new BlockPos(this.posX, this.posY, this.posZ));
            textureManager.bindTexture(FOOTPRINT_TEXTURE);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            ((BufferBuilder) vc).begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            vc.pos(x - radius, y, z + radius).tex(0, 1).color(light, light, light, alpha).endVertex();
            vc.pos(x + radius, y, z + radius).tex(1, 1).color(light, light, light, alpha).endVertex();
            vc.pos(x + radius, y, z - radius).tex(1, 0).color(light, light, light, alpha).endVertex();
            vc.pos(x - radius, y, z - radius).tex(0, 0).color(light, light, light, alpha).endVertex();
            Tessellator.getInstance().draw();
            RenderSystem.disableBlend();
            RenderSystem.enableLighting();
        }

        @Override
        public void tick() {
            ticks++;
            if (ticks == maxTicks)
                setExpired();
        }

        @Override
        public IParticleRenderType getRenderType() {
            return IParticleRenderType.CUSTOM;
        }

        public static class Factory implements IParticleFactory<BasicParticleType> {
            @Override
            public Particle makeParticle(BasicParticleType type, World world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
                return new FootprintParticle(Minecraft.getInstance().getTextureManager(), world, x, y, z);
            }
        }
    }

    private static class OldBlockDustParticle extends DiggingParticle {

        public OldBlockDustParticle(World world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, BlockState state) {
            super(world, x, y, z, xSpeed, ySpeed, zSpeed, state);
            motionX = xSpeed;
            motionY = ySpeed;
            motionZ = zSpeed;
        }

        public static class Factory implements IParticleFactory<BlockParticleData> {

            @Override
            public Particle makeParticle(BlockParticleData type, World world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
                return new OldBlockDustParticle(world, x, y, z, xSpeed, ySpeed, zSpeed, type.getBlockState());
            }
        }
    }

}
