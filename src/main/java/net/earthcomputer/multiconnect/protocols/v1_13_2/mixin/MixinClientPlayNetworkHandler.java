package net.earthcomputer.multiconnect.protocols.v1_13_2.mixin;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.impl.Constants;
import net.earthcomputer.multiconnect.impl.Utils;
import net.earthcomputer.multiconnect.protocols.generic.IUserDataHolder;
import net.earthcomputer.multiconnect.protocols.v1_13_2.*;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler {

    @Shadow public abstract void onOpenWrittenBook(OpenWrittenBookS2CPacket packet);

    @Shadow public abstract void onDifficulty(DifficultyS2CPacket packet);

    @Shadow public abstract void onSetTradeOffers(SetTradeOffersS2CPacket packet);

    @Shadow public abstract void onVelocityUpdate(EntityVelocityUpdateS2CPacket entityVelocityUpdateS2CPacket_1);

    @Shadow public abstract void onChunkLoadDistance(ChunkLoadDistanceS2CPacket packet);

    @Shadow public abstract void onChunkData(ChunkDataS2CPacket packet);

    @Unique private static final Logger MULTICONNECT_LOGGER = LogManager.getLogger("multiconnect");
    // Handle reordering of the (synthetic) render distance center packet, to allow the chunk data to arrive beforehand and queue it
    @Unique private boolean hasReceivedServerPosition;
    @Unique private int centerChunkX;
    @Unique private int centerChunkZ;
    @Unique private int viewDistance = 12;
    @Unique private static final int MAX_VIEW_DISTANCE = 32;
    @Unique private final Cache<ChunkPos, List<ChunkDataS2CPacket>> waitingChunkDataPackets = CacheBuilder.newBuilder()
            .expireAfterWrite(Constants.PACKET_QUEUE_DROP_TIMEOUT, TimeUnit.SECONDS)
            .removalListener((RemovalListener<ChunkPos, List<ChunkDataS2CPacket>>) notification -> {
                if (notification.wasEvicted()) {
                    MULTICONNECT_LOGGER.warn("Dropping chunk packet(s) at {}, {} because it was too far away from the render distance center {}, {}", notification.getKey().x, notification.getKey().z, centerChunkX, centerChunkZ);
                }
            })
            .build();
    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstruct(CallbackInfo ci) {
        Utils.autoCleanUp(waitingChunkDataPackets, Constants.PACKET_QUEUE_DROP_TIMEOUT, TimeUnit.SECONDS);
    }

    @Inject(method = "onChunkData", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift = At.Shift.AFTER), cancellable = true)
    private void onOnChunkData(ChunkDataS2CPacket packet, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_13_2) {
            if (!isInRange(packet.getX(), packet.getZ())) {
                waitingChunkDataPackets.asMap().computeIfAbsent(new ChunkPos(packet.getX(), packet.getZ()), k -> new ArrayList<>()).add(packet);
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
        if (ConnectionInfo.protocolVersion <= Protocols.V1_13_2 && hasReceivedServerPosition) {
            centerChunkX = packet.getChunkX();
            centerChunkZ = packet.getChunkZ();

            // Increase radius if necessary, up to a limit of MAX_VIEW_DISTANCE
            int newViewDistance = waitingChunkDataPackets.asMap().keySet().stream()
                    .mapToInt(pos -> getRequiredRange(pos.x, pos.z))
                    .filter(distance -> distance <= MAX_VIEW_DISTANCE)
                    .max().orElse(0);
            if (newViewDistance > viewDistance) {
                onChunkLoadDistance(new ChunkLoadDistanceS2CPacket(newViewDistance));
            }

            // Process queued packets
            for (int dx = centerChunkX - viewDistance; dx <= centerChunkX + viewDistance; dx++) {
                for (int dz = centerChunkZ - viewDistance; dz <= centerChunkZ + viewDistance; dz++) {
                    List<ChunkDataS2CPacket> chunkDataPackets = waitingChunkDataPackets.asMap().remove(new ChunkPos(dx, dz));
                    if (chunkDataPackets != null) {
                        for (ChunkDataS2CPacket chunkDataPacket : chunkDataPackets) {
                            onChunkData(chunkDataPacket);
                        }
                    }
                }
            }
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
                        trade.disable();
                    trades.add(trade);
                }
                onSetTradeOffers(new SetTradeOffersS2CPacket(syncId, trades, 5, 0, false, false));
                ci.cancel();
            } else if (Protocol_1_13_2.CUSTOM_PAYLOAD_OPEN_BOOK.equals(channel)) {
                OpenWrittenBookS2CPacket openBookPacket = new OpenWrittenBookS2CPacket(packet.getData());
                onOpenWrittenBook(openBookPacket);
                ci.cancel();
            }
        }
    }

    @Inject(method = "onGameJoin", at = @At("TAIL"))
    private void onOnGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_13_2) {
            onDifficulty(new DifficultyS2CPacket(((IUserDataHolder) (Object) packet).multiconnect_getUserData(Protocol_1_13_2.DIFFICULTY_KEY), false));
        }
    }

    @Inject(method = "onPlayerRespawn", at = @At("TAIL"))
    private void onOnPlayerRespawn(PlayerRespawnS2CPacket packet, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_13_2) {
            hasReceivedServerPosition = false;
            onDifficulty(new DifficultyS2CPacket(((IUserDataHolder) packet).multiconnect_getUserData(Protocol_1_13_2.DIFFICULTY_KEY), false));
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
            hasReceivedServerPosition = true;
            Protocol_1_13_2.updateCameraPosition();
        }
    }

}
