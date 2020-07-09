package net.earthcomputer.multiconnect.mixin.bridge;

import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.generic.ChunkData;
import net.earthcomputer.multiconnect.transformer.TransformerByteBuf;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkDataS2CPacket.class)
public class MixinChunkDataS2C {

    @Inject(method = "read", at = @At("HEAD"))
    private void onRead(PacketByteBuf buf, CallbackInfo ci) {
        ChunkData chunkData = ConnectionInfo.protocol.createChunkData();
        if (chunkData != null) {
            chunkData.read(buf);
            chunkData.writePendingReads((TransformerByteBuf) buf);
        }
    }

}
