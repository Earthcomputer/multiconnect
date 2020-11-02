package net.earthcomputer.multiconnect.protocols.v1_13_2.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.v1_13_2.*;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler {

    @Shadow public abstract void onLightUpdate(LightUpdateS2CPacket packet);

    @Shadow public abstract void onOpenWrittenBook(OpenWrittenBookS2CPacket packet);

    @Shadow public abstract void onDifficulty(DifficultyS2CPacket packet);

    @Shadow public abstract void onSetTradeOffers(SetTradeOffersS2CPacket packet);

    @Shadow public abstract void onVelocityUpdate(EntityVelocityUpdateS2CPacket entityVelocityUpdateS2CPacket_1);

    @Shadow public abstract void onChunkLoadDistance(ChunkLoadDistanceS2CPacket packet);

    @Shadow public abstract void onChunkData(ChunkDataS2CPacket packet);

    @Unique private static final Logger MULTICONNECT_LOGGER = LogManager.getLogger("multiconnect");
    // Handle reordering of the (synthetic) render distance center packet, to allow the chunk data to arrive beforehand and queue it
    @Unique private int centerChunkX;
    @Unique private int centerChunkZ;
    @Unique private int viewDistance = 12;
    @Unique private final List<ChunkDataS2CPacket> chunkDataPacketQueue = new ArrayList<>();
    @Unique private final List<LightUpdateS2CPacket> lightUpdatePacketQueue = new ArrayList<>();

    @Inject(method = "onChunkData", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift = At.Shift.AFTER), cancellable = true)
    private void onOnChunkData(ChunkDataS2CPacket packet, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_13_2) {
            if (!isInRange(packet.getX(), packet.getZ())) {
                chunkDataPacketQueue.add(packet);
                ci.cancel();
            }
        }
    }

    @Inject(method = "onLightUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift = At.Shift.AFTER), cancellable = true)
    private void onOnLightUpdate(LightUpdateS2CPacket packet, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_13_2) {
            if (!isInRange(packet.getChunkX(), packet.getChunkZ())) {
                lightUpdatePacketQueue.add(packet);
                ci.cancel();
            }
        }
    }

    @Inject(method = "onChunkLoadDistance", at = @At("RETURN"))
    private void onOnChunkLoadDistance(ChunkLoadDistanceS2CPacket packet, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_13_2) {
            viewDistance = packet.getDistance();
        }
    }

    @Inject(method = "onChunkRenderDistanceCenter", at = @At("RETURN"))
    private void onOnRenderDistanceCenter(ChunkRenderDistanceCenterS2CPacket packet, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_13_2) {
            centerChunkX = packet.getChunkX();
            centerChunkZ = packet.getChunkZ();

            // Increase radius if necessary, up to a limit of 32
            int newViewDistance = IntStream.concat(
                    chunkDataPacketQueue.stream().mapToInt(chunkData -> getRequiredRange(chunkData.getX(), chunkData.getZ())),
                    lightUpdatePacketQueue.stream().mapToInt(lightUpdate -> getRequiredRange(lightUpdate.getChunkX(), lightUpdate.getChunkZ()))
            ).filter(distance -> distance <= 32).max().orElse(0);
            if (newViewDistance > viewDistance) {
                onChunkLoadDistance(new ChunkLoadDistanceS2CPacket(newViewDistance));
            }

            // Process queued packets
            for (ChunkDataS2CPacket chunkData : chunkDataPacketQueue) {
                if (isInRange(chunkData.getX(), chunkData.getZ())) {
                    onChunkData(chunkData);
                } else {
                    MULTICONNECT_LOGGER.warn("Dropping chunk packet at {}, {} because it was too far away from the render distance center {}, {}", chunkData.getX(), chunkData.getZ(), centerChunkX, centerChunkZ);
                }
            }
            chunkDataPacketQueue.clear();

            for (LightUpdateS2CPacket lightUpdate : lightUpdatePacketQueue) {
                if (isInRange(lightUpdate.getChunkX(), lightUpdate.getChunkZ())) {
                    onLightUpdate(lightUpdate);
                } else {
                    MULTICONNECT_LOGGER.warn("Dropping light update packet at {}, {} because it was too far away from the render distance center {}, {}", lightUpdate.getChunkX(), lightUpdate.getChunkZ(), centerChunkX, centerChunkZ);
                }
            }
            lightUpdatePacketQueue.clear();
        }
    }

    @Unique
    private boolean isInRange(int chunkX, int chunkZ) {
        return getRequiredRange(chunkX, chunkZ) <= viewDistance;
    }

    @Unique
    private int getRequiredRange(int chunkX, int chunkZ) {
        return Math.max(Math.abs(chunkX - centerChunkX), Math.abs(chunkZ - centerChunkZ));
    }

    @Inject(method = "onCustomPayload", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift = At.Shift.AFTER), cancellable = true)
    private void onOnCustomPayload(CustomPayloadS2CPacket packet, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_13_2) {
            Identifier channel = packet.getChannel();
            if (Protocol_1_13_2.CUSTOM_PAYLOAD_TRADE_LIST.equals(channel)) {
                PacketByteBuf buf = packet.getData();
                int syncId = buf.readInt();
                TradeOfferList trades = new TradeOfferList();
                int tradeCount = buf.readUnsignedByte();
                for (int i = 0; i < tradeCount; i++) {
                    ItemStack buy = buf.readItemStack();
                    ItemStack sell = buf.readItemStack();
                    boolean hasSecondItem = buf.readBoolean();
                    ItemStack secondBuy = hasSecondItem ? buf.readItemStack() : ItemStack.EMPTY;
                    boolean locked = buf.readBoolean();
                    int tradeUses = buf.readInt();
                    int maxTradeUses = buf.readInt();
                    TradeOffer trade = new TradeOffer(buy, secondBuy, sell, tradeUses, maxTradeUses, 0, 1);
                    if (locked)
                        trade.clearUses();
                    trades.add(trade);
                }
                onSetTradeOffers(new SetTradeOffersS2CPacket(syncId, trades, 5, 0, false, false));
                ci.cancel();
            } else if (Protocol_1_13_2.CUSTOM_PAYLOAD_OPEN_BOOK.equals(channel)) {
                OpenWrittenBookS2CPacket openBookPacket = new OpenWrittenBookS2CPacket();
                try {
                    openBookPacket.read(packet.getData());
                } catch (IOException e) {
                    MULTICONNECT_LOGGER.error("Failed to read open book packet", e);
                }
                onOpenWrittenBook(openBookPacket);
                ci.cancel();
            }
        }
    }

    @Inject(method = "onGameJoin", at = @At("TAIL"))
    private void onOnGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_13_2) {
            onDifficulty(new DifficultyS2CPacket(PendingDifficulty.getPendingDifficulty(), false));
        }
    }

    @Inject(method = "onPlayerRespawn", at = @At("TAIL"))
    private void onOnPlayerRespawn(PlayerRespawnS2CPacket packet, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_13_2) {
            onDifficulty(new DifficultyS2CPacket(PendingDifficulty.getPendingDifficulty(), false));
        }
    }

    @Inject(method = "onEntitySpawn", at = @At("TAIL"))
    private void onOnEntitySpawn(EntitySpawnS2CPacket packet, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_13_2) {
            if (packet.getEntityTypeId() == EntityType.ITEM
                    || packet.getEntityTypeId() == EntityType.ARROW
                    || packet.getEntityTypeId() == EntityType.SPECTRAL_ARROW
                    || packet.getEntityTypeId() == EntityType.TRIDENT) {
                onVelocityUpdate(new EntityVelocityUpdateS2CPacket(packet.getId(),
                        new Vec3d(packet.getVelocityX(), packet.getVelocityY(), packet.getVelocityZ())));
            }
        }
    }

    @Inject(method = "onPlayerPositionLook", at = @At("TAIL"))
    private void onOnPlayerPositionLook(PlayerPositionLookS2CPacket packet, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_13_2) {
            Protocol_1_13_2.updateCameraPosition();
        }
    }

}
