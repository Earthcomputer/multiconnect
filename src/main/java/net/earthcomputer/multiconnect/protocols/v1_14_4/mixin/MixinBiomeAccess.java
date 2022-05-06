package net.earthcomputer.multiconnect.protocols.v1_14_4.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.v1_14_4.IBiomeStorage_1_14_4;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(BiomeAccess.class)
public class MixinBiomeAccess {
    @Unique private static final Logger MULTICONNECT_LOGGER = LogManager.getLogger("multiconnect");
    @Unique private final AtomicBoolean multiconnect_hasWarned = new AtomicBoolean();
    @Shadow @Final private BiomeAccess.Storage storage;

    @Inject(method = "getBiome", at = @At("HEAD"), cancellable = true)
    private void onGetBiome(BlockPos pos, CallbackInfoReturnable<Biome> cir) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_14_4) {
            if (storage instanceof IBiomeStorage_1_14_4 storage114) {
                cir.setReturnValue(storage114.multiconnect_getBiome_1_14_4(pos.getX(), pos.getZ()));
            } else if (!multiconnect_hasWarned.getAndSet(true)) {
                MULTICONNECT_LOGGER.warn("Unsupported biome storage type for 1.14 and below: {}.",
                        storage.getClass().getName());
            }
        }
    }
}
