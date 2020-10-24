package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.v1_8.DataTrackerEntry_1_8;
import net.earthcomputer.multiconnect.protocols.v1_8.Protocol_1_8;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EulerAngle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientPlayNetworkHandler.class, priority = -1000)
public class MixinClientPlayNetworkHandler {
    @Unique private static final Logger MULTICONNECT_LOGGER = LogManager.getLogger("multiconnect");

    @Shadow private ClientWorld world;

    @Inject(method = "onEntityTrackerUpdate",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift = At.Shift.AFTER),
            cancellable = true)
    private void onOnEntityTrackerUpdate(EntityTrackerUpdateS2CPacket packet, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_8) {
            Entity entity = world.getEntityById(packet.id());
            if (entity != null) {
                for (DataTracker.Entry<?> entry : packet.getTrackedValues()) {
                    if (!(entry instanceof DataTrackerEntry_1_8)) {
                        MULTICONNECT_LOGGER.warn("Not handling entity tracker update entry which was not constructed for 1.8");
                        continue;
                    }
                    DataTrackerEntry_1_8 entry_1_8 = (DataTrackerEntry_1_8) entry;
                    switch (entry_1_8.getSerializerId()) {
                        case 0:
                            Protocol_1_8.handleByteTrackedData(entity, entry_1_8.getId(), (Byte) entry_1_8.get());
                            break;
                        case 1:
                            Protocol_1_8.handleShortTrackedData(entity, entry_1_8.getId(), (Short) entry_1_8.get());
                            break;
                        case 2:
                            Protocol_1_8.handleIntTrackedData(entity, entry_1_8.getId(), (Integer) entry_1_8.get());
                            break;
                        case 3:
                            Protocol_1_8.handleFloatTrackedData(entity, entry_1_8.getId(), (Float) entry_1_8.get());
                            break;
                        case 4:
                            Protocol_1_8.handleStringTrackedData(entity, entry_1_8.getId(), (String) entry_1_8.get());
                            break;
                        case 5:
                            Protocol_1_8.handleItemStackTrackedData(entity, entry_1_8.getId(), (ItemStack) entry_1_8.get());
                            break;
                        case 6:
                            Protocol_1_8.handleBlockPosTrackedData(entity, entry_1_8.getId(), (BlockPos) entry_1_8.get());
                            break;
                        case 7:
                            Protocol_1_8.handleEulerAngleTrackedData(entity, entry_1_8.getId(), (EulerAngle) entry_1_8.get());
                            break;
                        default:
                            throw new AssertionError();
                    }
                }
            }
            ci.cancel();
        }
    }
}
