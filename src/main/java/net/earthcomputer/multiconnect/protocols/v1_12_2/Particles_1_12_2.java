package net.earthcomputer.multiconnect.protocols.v1_12_2;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.earthcomputer.multiconnect.protocols.generic.IParticleManager;
import net.earthcomputer.multiconnect.protocols.generic.MyParticleType;
import net.earthcomputer.multiconnect.protocols.generic.RegistryBuilder;
import net.earthcomputer.multiconnect.protocols.v1_12_2.mixin.CrackParticleAccessor;
import net.earthcomputer.multiconnect.protocols.v1_12_2.mixin.SuspendParticleAccessor;
import net.earthcomputer.multiconnect.protocols.v1_17_1.Particles_1_17_1;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
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

import static net.minecraft.particle.ParticleTypes.*;

public class Particles_1_12_2 {

    private static final BiMap<ParticleType<?>, String> OLD_NAMES = HashBiMap.create();

    public static final DefaultParticleType DEPTH_SUSPEND = new MyParticleType(false);
    public static final DefaultParticleType FOOTSTEP = new MyParticleType(false);
    public static final DefaultParticleType SNOW_SHOVEL = new MyParticleType(false);
    public static final ParticleType<BlockStateParticleEffect> BLOCK_DUST = new MyBlockStateParticleType(false);
    public static final DefaultParticleType TAKE = new MyParticleType(false);

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
        OLD_NAMES.put(Particles_1_17_1.BARRIER, "barrier");
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

    public static void registerParticles(RegistryBuilder<ParticleType<?>> registry) {
        registry.disableSideEffects();
        registry.clear();
        registry.registerInPlace(0, POOF, "poof");
        registry.registerInPlace(1, EXPLOSION, "explosion");
        registry.registerInPlace(2, EXPLOSION_EMITTER, "explosion_emitter");
        registry.registerInPlace(3, FIREWORK, "firework");
        registry.registerInPlace(4, BUBBLE, "bubble");
        registry.registerInPlace(5, SPLASH, "splash");
        registry.registerInPlace(6, FISHING, "fishing");
        registry.registerInPlace(7, UNDERWATER, "underwater");
        registry.registerInPlace(8, DEPTH_SUSPEND, "depth_suspend");
        registry.registerInPlace(9, CRIT, "crit");
        registry.registerInPlace(10, ENCHANTED_HIT, "enchanted_hit");
        registry.registerInPlace(11, SMOKE, "smoke");
        registry.registerInPlace(12, LARGE_SMOKE, "large_smoke");
        registry.registerInPlace(13, EFFECT, "effect");
        registry.registerInPlace(14, INSTANT_EFFECT, "instant_effect");
        registry.registerInPlace(15, ENTITY_EFFECT, "entity_effect");
        registry.registerInPlace(16, AMBIENT_ENTITY_EFFECT, "ambient_entity_effect");
        registry.registerInPlace(17, WITCH, "witch");
        registry.registerInPlace(18, DRIPPING_WATER, "dripping_water");
        registry.registerInPlace(19, DRIPPING_LAVA, "dripping_lava");
        registry.registerInPlace(20, ANGRY_VILLAGER, "angry_villager");
        registry.registerInPlace(21, HAPPY_VILLAGER, "happy_villager");
        registry.registerInPlace(22, MYCELIUM, "mycelium");
        registry.registerInPlace(23, NOTE, "note");
        registry.registerInPlace(24, PORTAL, "portal");
        registry.registerInPlace(25, ENCHANT, "enchant");
        registry.registerInPlace(26, FLAME, "flame");
        registry.registerInPlace(27, LAVA, "lava");
        registry.registerInPlace(28, FOOTSTEP, "footstep");
        registry.registerInPlace(29, CLOUD, "cloud");
        registry.registerInPlace(30, DUST, "dust");
        registry.registerInPlace(31, ITEM_SNOWBALL, "item_snowball");
        registry.registerInPlace(32, SNOW_SHOVEL, "snow_shovel");
        registry.registerInPlace(33, ITEM_SLIME, "item_slime");
        registry.registerInPlace(34, HEART, "heart");
        registry.registerInPlace(35, Particles_1_17_1.BARRIER, "barrier");
        registry.registerInPlace(36, ITEM, "item");
        registry.registerInPlace(37, BLOCK, "block");
        registry.registerInPlace(38, BLOCK_DUST, "block_dust");
        registry.registerInPlace(39, RAIN, "rain");
        registry.registerInPlace(40, TAKE, "take"); // this particle was added in 1.8 and never had any factory
        registry.registerInPlace(41, ELDER_GUARDIAN, "elder_guardian");
        registry.registerInPlace(42, DRAGON_BREATH, "dragon_breath");
        registry.registerInPlace(43, END_ROD, "end_rod");
        registry.registerInPlace(44, DAMAGE_INDICATOR, "damage_indicator");
        registry.registerInPlace(45, SWEEP_ATTACK, "sweep_attack");
        registry.registerInPlace(46, FALLING_DUST, "falling_dust");
        registry.registerInPlace(47, TOTEM_OF_UNDYING, "totem_of_undying");
        registry.registerInPlace(48, SPIT, "spit");
        registry.enableSideEffects();

        registerParticleFactories((IParticleManager) MinecraftClient.getInstance().particleManager);
    }

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
