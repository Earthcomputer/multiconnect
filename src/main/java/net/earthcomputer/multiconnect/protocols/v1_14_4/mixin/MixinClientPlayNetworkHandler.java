package net.earthcomputer.multiconnect.protocols.v1_14_4.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.v1_14_4.PendingDataTrackerEntries;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.packet.EntityTrackerUpdateS2CPacket;
import net.minecraft.client.network.packet.MobSpawnS2CPacket;
import net.minecraft.client.network.packet.PlayerSpawnS2CPacket;
import net.minecraft.entity.data.DataTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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

    @Unique
    private void applyPendingEntityTrackerValues(int entityId) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_14_4) {
            List<DataTracker.Entry<?>> entries = PendingDataTrackerEntries.getEntries(entityId);
            if (entries != null) {
                PendingDataTrackerEntries.setEntries(entityId, null);
                EntityTrackerUpdateS2CPacket trackerPacket = new EntityTrackerUpdateS2CPacket();
                //noinspection ConstantConditions
                TrackerUpdatePacketAccessor trackerPacketAccessor = (TrackerUpdatePacketAccessor) trackerPacket;
                trackerPacketAccessor.setId(entityId);
                trackerPacketAccessor.setTrackedValues(entries);
                onEntityTrackerUpdate(trackerPacket);
            }
        }
    }

}
