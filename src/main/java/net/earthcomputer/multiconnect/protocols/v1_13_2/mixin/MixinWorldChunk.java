package net.earthcomputer.multiconnect.protocols.v1_13_2.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.v1_13_2.Protocol_1_13_2;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldChunk.class)
public class MixinWorldChunk {
    @Inject(method = "loadFromPacket", at = @At("RETURN"))
    private void recalculateHeightmaps(CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_13_2) {
            WorldChunk chunk = (WorldChunk) (Object) this;
            for (ChunkSection section : chunk.getSectionArray()) {
                if (section != null) {
                    section.calculateCounts();
                }
            }
            Heightmap.populateHeightmaps(chunk, Protocol_1_13_2.CLIENT_HEIGHTMAPS);
        }
    }
}
