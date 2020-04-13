package net.earthcomputer.multiconnect.protocols.v1_14_4.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.v1_14_4.IBiomeStorage_1_14_4;
import net.earthcomputer.multiconnect.protocols.v1_14_4.PendingBiomeData;
import net.minecraft.client.multiplayer.ClientChunkProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeContainer;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientChunkProvider.class)
public class MixinClientChunkManager {

    @Inject(method = "loadChunk(IILnet/minecraft/world/biome/BiomeContainer;Lnet/minecraft/network/PacketBuffer;Lnet/minecraft/nbt/CompoundNBT;I)Lnet/minecraft/world/chunk/Chunk;", at = @At("RETURN"))
    private void onLoadChunkFromPacket(int x, int z, BiomeContainer biomeArray, PacketBuffer buf, CompoundNBT heightmaps, int verticalStripMask, CallbackInfoReturnable<Chunk> ci) {
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
