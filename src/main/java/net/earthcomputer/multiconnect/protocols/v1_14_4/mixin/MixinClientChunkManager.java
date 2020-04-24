package net.earthcomputer.multiconnect.protocols.v1_14_4.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.v1_14_4.IBiomeStorage_1_14_4;
import net.earthcomputer.multiconnect.protocols.v1_14_4.PendingBiomeData;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeArray;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientChunkManager.class)
public class MixinClientChunkManager {

    @Inject(method = "loadChunkFromPacket", at = @At("RETURN"))
    private void onLoadChunkFromPacket(int x, int z, BiomeArray biomeArray, PacketByteBuf buf, CompoundTag heightmaps, int verticalStripMask, boolean bl, CallbackInfoReturnable<WorldChunk> ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_14_4) {
            if (ci.getReturnValue() != null) {
                Biome[] biomeData = PendingBiomeData.getPendingBiomeData(x, z);
                if (biomeData != null) {
                    ((IBiomeStorage_1_14_4) ci.getReturnValue()).multiconnect_setBiomeArray_1_14_4(biomeData);
                    PendingBiomeData.setPendingBiomeData(x, z, null);
                }
            }
        }
    }

}
