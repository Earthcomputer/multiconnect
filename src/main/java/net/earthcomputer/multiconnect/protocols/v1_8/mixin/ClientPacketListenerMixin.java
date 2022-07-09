package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import com.mojang.logging.LogUtils;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.impl.PacketSystem;
import net.earthcomputer.multiconnect.impl.Utils;
import net.earthcomputer.multiconnect.protocols.v1_16.Protocol_1_16_5;
import net.earthcomputer.multiconnect.protocols.v1_17.Protocol_1_17_1;
import net.earthcomputer.multiconnect.protocols.v1_8.DataTrackerEntry_1_8;
import net.earthcomputer.multiconnect.protocols.v1_8.Protocol_1_8;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Rotations;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = ClientPacketListener.class, priority = -1000)
public abstract class ClientPacketListenerMixin {
    @Unique private static final Logger MULTICONNECT_LOGGER = LogUtils.getLogger();

    @Shadow private ClientLevel level;

    @Shadow public abstract void handleForgetLevelChunk(ClientboundForgetLevelChunkPacket packet);

    @Shadow public abstract void handleEntityEvent(ClientboundEntityEventPacket packet);

    @Shadow public abstract void handleLevelChunkWithLight(ClientboundLevelChunkWithLightPacket packet);

    @Shadow private RegistryAccess.Frozen registryAccess;

    @Inject(method = {"handleLogin", "handleRespawn"}, at = @At("TAIL"))
    private void onOnGameJoinOrRespawn(CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_8) {
            // client permission level 4 to enable features just in case we're opped
            LocalPlayer player = Minecraft.getInstance().player;
            assert player != null;
            handleEntityEvent(new ClientboundEntityEventPacket(player, (byte) 28));
        }
    }

    @Inject(method = "handleLevelChunkWithLight", at = @At("HEAD"), cancellable = true)
    private void onOnChunkData(ClientboundLevelChunkWithLightPacket packet, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_8
                && PacketSystem.getUserData(packet).get(Protocol_1_16_5.FULL_CHUNK_KEY)
                && PacketSystem.getUserData(packet).get(Protocol_1_17_1.VERTICAL_STRIP_BITMASK).isEmpty()) {
            handleForgetLevelChunk(new ClientboundForgetLevelChunkPacket(packet.getX(), packet.getZ()));
            ci.cancel();
        }
    }

    @Inject(method = "handleLevelChunkWithLight", at = @At("RETURN"))
    private void postChunkData(ClientboundLevelChunkWithLightPacket packet, CallbackInfo ci) {
        // 1.8 doesn't send neighboring empty chunks, so we must assume they are empty unless otherwise specified
        if (ConnectionInfo.protocolVersion > Protocols.V1_8) {
            return;
        }

        // don't load more empty chunks next to empty chunks, that would cause an infinite loop
        LevelChunk chunk = level.getChunk(packet.getX(), packet.getZ());
        if (!Utils.isChunkEmpty(chunk)) {
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                int x = packet.getX() + dir.getStepX();
                int z = packet.getZ() + dir.getStepZ();
                if (level.getChunk(x, z, ChunkStatus.FULL, false) == null) {
                    handleLevelChunkWithLight(Utils.createEmptyChunkDataPacket(x, z, level, registryAccess));
                }
            }
        }
    }

    @Inject(method = "handleSetEntityData",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/util/thread/BlockableEventLoop;)V", shift = At.Shift.AFTER),
            cancellable = true)
    private void onOnEntityTrackerUpdate(ClientboundSetEntityDataPacket packet, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_8) {
            Entity entity = level.getEntity(packet.getId());
            if (entity != null) {
                List<SynchedEntityData.DataItem<?>> trackedValues = packet.getUnpackedData();
                if (trackedValues == null) {
                    return;
                }
                for (SynchedEntityData.DataItem<?> entry : trackedValues) {
                    if (!(entry instanceof DataTrackerEntry_1_8 entry_1_8)) {
                        MULTICONNECT_LOGGER.warn("Not handling entity tracker update entry which was not constructed for 1.8");
                        continue;
                    }
                    switch (entry_1_8.getSerializerId()) {
                        case 0 -> Protocol_1_8.handleByteTrackedData(entity, entry_1_8.getId(), (Byte) entry_1_8.getValue());
                        case 1 -> Protocol_1_8.handleShortTrackedData(entity, entry_1_8.getId(),
                                (Short) entry_1_8.getValue());
                        case 2 -> Protocol_1_8.handleIntTrackedData(entity, entry_1_8.getId(),
                                (Integer) entry_1_8.getValue());
                        case 3 -> Protocol_1_8.handleFloatTrackedData(entity, entry_1_8.getId(),
                                (Float) entry_1_8.getValue());
                        case 4 -> Protocol_1_8.handleStringTrackedData(entity, entry_1_8.getId(),
                                (String) entry_1_8.getValue());
                        case 5 -> Protocol_1_8.handleItemStackTrackedData(entity, entry_1_8.getId(),
                                (ItemStack) entry_1_8.getValue());
                        case 6 -> Protocol_1_8.handleBlockPosTrackedData(entity, entry_1_8.getId(), (BlockPos) entry_1_8.getValue());
                        case 7 -> Protocol_1_8.handleEulerAngleTrackedData(entity, entry_1_8.getId(), (Rotations) entry_1_8.getValue());
                        default -> throw new AssertionError();
                    }
                }
            }
            ci.cancel();
        }
    }
}
