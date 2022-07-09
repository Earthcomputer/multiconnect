package net.earthcomputer.multiconnect.protocols.v1_17;

import net.earthcomputer.multiconnect.debug.DebugUtils;
import net.earthcomputer.multiconnect.protocols.generic.IParticleManager;
import net.earthcomputer.multiconnect.protocols.generic.MyParticleType;
import net.earthcomputer.multiconnect.protocols.v1_17.mixin.BlockMarkerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.block.Blocks;

public class Particles_1_17_1 {
    public static final SimpleParticleType BARRIER = new MyParticleType(false);
    public static final SimpleParticleType LIGHT = new MyParticleType(false);

    public static void register() {
        Registry.register(Registry.PARTICLE_TYPE, "multiconnect:barrier", BARRIER);
        Registry.register(Registry.PARTICLE_TYPE, "multiconnect:light", LIGHT);
    }

    public static void registerFactories() {
        if (!DebugUtils.UNIT_TEST_MODE) {
            IParticleManager particleManager = (IParticleManager) Minecraft.getInstance().particleEngine;
            particleManager.multiconnect_registerProvider(BARRIER, (p, w, x, y, z, vx, vy, vz) ->
                    BlockMarkerAccessor.createBlockMarkerParticle(w, x, y, z, Blocks.BARRIER.defaultBlockState()));
            particleManager.multiconnect_registerProvider(LIGHT, (p, w, x, y, z, vx, vy, vz) ->
                    BlockMarkerAccessor.createBlockMarkerParticle(w, x, y, z, Blocks.LIGHT.defaultBlockState()));
        }
    }
}
