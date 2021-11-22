package net.earthcomputer.multiconnect.protocols.v1_17_1;

import net.earthcomputer.multiconnect.impl.DebugUtils;
import net.earthcomputer.multiconnect.impl.Utils;
import net.earthcomputer.multiconnect.protocols.generic.IParticleManager;
import net.earthcomputer.multiconnect.protocols.generic.ISimpleRegistry;
import net.earthcomputer.multiconnect.protocols.generic.MyParticleType;
import net.earthcomputer.multiconnect.protocols.v1_17_1.mixin.BlockMarkerParticleAccessor;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;

public class Particles_1_17_1 {
    public static final DefaultParticleType BARRIER = new MyParticleType(false);
    public static final DefaultParticleType LIGHT = new MyParticleType(false);

    public static void mutateParticleTypeRegistry(ISimpleRegistry<ParticleType<?>> registry) {
        registry.unregister(ParticleTypes.BLOCK_MARKER);
        Utils.insertAfter(registry, ParticleTypes.ANGRY_VILLAGER, BARRIER, "barrier");
        Utils.insertAfter(registry, BARRIER, LIGHT, "light");

        if (!DebugUtils.UNIT_TEST_MODE) {
            IParticleManager particleManager = (IParticleManager) MinecraftClient.getInstance().particleManager;
            particleManager.multiconnect_registerFactory(BARRIER, (p, w, x, y, z, vx, vy, vz) ->
                    BlockMarkerParticleAccessor.createBlockMarkerParticle(w, x, y, z, Blocks.BARRIER.getDefaultState()));
            particleManager.multiconnect_registerFactory(LIGHT, (p, w, x, y, z, vx, vy, vz) ->
                    BlockMarkerParticleAccessor.createBlockMarkerParticle(w, x, y, z, Blocks.LIGHT.getDefaultState()));
        }
    }
}
