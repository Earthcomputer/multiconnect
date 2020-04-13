package net.earthcomputer.multiconnect.protocols.v1_14_4.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.v1_14_4.PendingDataTrackerEntries;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SEntityMetadataPacket;
import net.minecraft.network.play.server.SSpawnMobPacket;
import net.minecraft.network.play.server.SSpawnPlayerPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ClientPlayNetHandler.class)
public abstract class MixinClientPlayNetworkHandler {

    @Shadow public abstract void handleEntityMetadata(SEntityMetadataPacket packet);

    @Inject(method = "handleSpawnMob", at = @At("RETURN"))
    private void onOnMobSpawn(SSpawnMobPacket packet, CallbackInfo ci) {
        applyPendingEntityTrackerValues(packet.getEntityID());
    }

    @Inject(method = "handleSpawnPlayer", at = @At("RETURN"))
    private void onOnPlayerSpawn(SSpawnPlayerPacket packet, CallbackInfo ci) {
        applyPendingEntityTrackerValues(packet.getEntityID());
    }

    @Unique
    private void applyPendingEntityTrackerValues(int entityId) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_14_4) {
            List<EntityDataManager.DataEntry<?>> entries = PendingDataTrackerEntries.getRegistryObjects(entityId);
            if (entries != null) {
                PendingDataTrackerEntries.setEntries(entityId, null);
                SEntityMetadataPacket trackerPacket = new SEntityMetadataPacket();
                //noinspection ConstantConditions
                TrackerUpdatePacketAccessor trackerPacketAccessor = (TrackerUpdatePacketAccessor) trackerPacket;
                trackerPacketAccessor.setEntityId(entityId);
                trackerPacketAccessor.setDataManagerEntries(entries);
                handleEntityMetadata(trackerPacket);
            }
        }
    }

}
