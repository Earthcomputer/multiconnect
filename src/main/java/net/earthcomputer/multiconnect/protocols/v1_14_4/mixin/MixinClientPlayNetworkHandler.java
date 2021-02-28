package net.earthcomputer.multiconnect.protocols.v1_14_4.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.v1_14_4.IBiomeStorage_1_14_4;
import net.earthcomputer.multiconnect.protocols.v1_14_4.IChunkDataS2CPacket;
import net.earthcomputer.multiconnect.protocols.v1_14_4.PendingDataTrackerEntries;
import net.earthcomputer.multiconnect.transformer.TransformerByteBuf;
import net.earthcomputer.multiconnect.transformer.UnsignedByte;
import net.earthcomputer.multiconnect.transformer.VarInt;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.MobSpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerSpawnS2CPacket;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler {

    @Shadow public abstract void onEntityTrackerUpdate(EntityTrackerUpdateS2CPacket packet);

    @Inject(method = "onMobSpawn", at = @At("RETURN"))
    private void onOnMobSpawn(MobSpawnS2CPacket packet, CallbackInfo ci) {
        applyPendingEntityTrackerValues(packet.getId());
    }

    @Inject(method = "onPlayerSpawn", at = @At("RETURN"))
    private void onOnPlayerSpawn(PlayerSpawnS2CPacket packet, CallbackInfo ci) {
        applyPendingEntityTrackerValues(packet.getId());
    }

    @ModifyVariable(method = "onChunkData", ordinal = 0, at = @At(value = "STORE", ordinal = 0))
    private WorldChunk setBiomeArray(WorldChunk chunk, ChunkDataS2CPacket packet) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_14_4) {
            if (chunk != null) {
                Biome[] biomeData = ((IChunkDataS2CPacket) packet).get_1_14_4_biomeData();
                if (biomeData != null) {
                    ((IBiomeStorage_1_14_4) chunk).multiconnect_setBiomeArray_1_14_4(biomeData);
                }
            }
        }
        return chunk;
    }

    @Unique
    private void applyPendingEntityTrackerValues(int entityId) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_14_4) {
            List<DataTracker.Entry<?>> entries = PendingDataTrackerEntries.getEntries(entityId);
            if (entries != null) {
                PendingDataTrackerEntries.setEntries(entityId, null);
                TransformerByteBuf buf = TransformerByteBuf.forPacketConstruction(EntityTrackerUpdateS2CPacket.class, Protocols.V1_15);
                buf.pendingRead(VarInt.class, new VarInt(entityId));
                buf.pendingRead(UnsignedByte.class, new UnsignedByte((short)255)); // terminating byte
                buf.applyPendingReads();
                EntityTrackerUpdateS2CPacket packet = new EntityTrackerUpdateS2CPacket(buf);
                //noinspection ConstantConditions
                ((TrackerUpdatePacketAccessor) packet).setTrackedValues(entries);
                onEntityTrackerUpdate(packet);
            }
        }
    }

}
