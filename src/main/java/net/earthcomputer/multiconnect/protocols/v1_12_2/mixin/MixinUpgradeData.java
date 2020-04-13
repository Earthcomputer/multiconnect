package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.v1_12_2.IUpgradableChunk;
import net.minecraft.util.palette.UpgradeData;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(UpgradeData.class)
public class MixinUpgradeData {

    @Redirect(method = "func_196991_a", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/Chunk;getUpgradeData()Lnet/minecraft/util/palette/UpgradeData;"))
    private static UpgradeData redirectGetUpgradeData(Chunk chunk) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2) {
            return ((IUpgradableChunk) chunk).multiconnect_getClientUpgradeData();
        }
        return chunk.getUpgradeData();
    }

}
