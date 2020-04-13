package net.earthcomputer.multiconnect.protocols.v1_13_2.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.v1_13_2.*;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MerchantOffer;
import net.minecraft.item.MerchantOffers;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.util.ArrayList;

@Mixin(ClientPlayNetHandler.class)
public abstract class MixinClientPlayNetworkHandler {

    @Shadow @Final private static Logger LOGGER;
    @Shadow private ClientWorld world;

    @Shadow public abstract void handleUpdateLight(SUpdateLightPacket packet);

    @Shadow public abstract void handleOpenBookPacket(SOpenBookWindowPacket packet);

    @Shadow public abstract void handleServerDifficulty(SServerDifficultyPacket packet);

    @Shadow public abstract void handleMerchantOffers(SMerchantOffersPacket packet);

    @Shadow public abstract void handleEntityVelocity(SEntityVelocityPacket entityVelocityUpdateS2CPacket_1);

    @Inject(method = "handleChunkData", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/play/server/SChunkDataPacket;getReadBuffer()Lnet/minecraft/network/PacketBuffer;", shift = At.Shift.AFTER))
    private void onChunkDataPost(SChunkDataPacket packet, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_13_2) {
            if (!PendingChunkDataPackets.isProcessingQueuedPackets()) {
                SUpdateLightPacket lightPacket = new SUpdateLightPacket();
                //noinspection ConstantConditions
                LightUpdatePacketAccessor lightPacketAccessor = (LightUpdatePacketAccessor) lightPacket;

                PendingLightData lightData = PendingLightData.getInstance(packet.getChunkX(), packet.getChunkZ());

                lightPacketAccessor.setChunkX(packet.getChunkX());
                lightPacketAccessor.setChunkZ(packet.getChunkZ());

                int blockLightMask = packet.getAvailableSections() << 1;
                int skyLightMask = world.dimension.hasSkyLight() ? blockLightMask : 0;
                lightPacketAccessor.setBlockLightUpdateMask(blockLightMask);
                lightPacketAccessor.setSkyLightUpdateMask(skyLightMask);
                lightPacketAccessor.setBlockLightData(new ArrayList<>());
                lightPacketAccessor.setSkyLightData(new ArrayList<>());

                for (int i = 0; i < 16; i++) {
                    byte[] blockData = lightData.getBlockLight(i);
                    if (blockData != null)
                        lightPacket.getBlockLightData().add(blockData);
                    byte[] skyData = lightData.getSkyLight(i);
                    if (skyData != null)
                        lightPacket.getSkyLightData().add(skyData);
                }

                PendingLightData.setInstance(packet.getChunkX(), packet.getChunkZ(), null);

                handleUpdateLight(lightPacket);
            }

            if (packet.isFullChunk())
                PendingChunkDataPackets.push(packet);
        }
    }

    @Inject(method = "handleChunkData", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;addEntitiesToChunk(Lnet/minecraft/world/chunk/Chunk;)V"))
    private void onChunkDataSuccess(SChunkDataPacket packet, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_13_2) {
            PendingChunkDataPackets.pop();
        }
    }

    @Inject(method = "handleCustomPayload", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/play/server/SCustomPayloadPlayPacket;getChannelName()Lnet/minecraft/util/ResourceLocation;"), cancellable = true)
    private void onOnCustomPayload(SCustomPayloadPlayPacket packet, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_13_2) {
            ResourceLocation channel = packet.getChannelName();
            if (Protocol_1_13_2.CUSTOM_PAYLOAD_TRADE_LIST.equals(channel)) {
                PacketBuffer buf = packet.getBufferData();
                int syncId = buf.readInt();
                MerchantOffers trades = new MerchantOffers();
                int tradeCount = buf.readUnsignedByte();
                for (int i = 0; i < tradeCount; i++) {
                    ItemStack buy = buf.readItemStack();
                    ItemStack sell = buf.readItemStack();
                    boolean hasSecondItem = buf.readBoolean();
                    ItemStack secondBuy = hasSecondItem ? buf.readItemStack() : ItemStack.EMPTY;
                    boolean locked = buf.readBoolean();
                    int tradeUses = buf.readInt();
                    int maxTradeUses = buf.readInt();
                    MerchantOffer trade = new MerchantOffer(buy, secondBuy, sell, tradeUses, maxTradeUses, 0, 1);
                    if (locked)
                        trade.resetUses();
                    trades.add(trade);
                }
                handleMerchantOffers(new SMerchantOffersPacket(syncId, trades, 5, 0, false, false));
                ci.cancel();
            } else if (Protocol_1_13_2.CUSTOM_PAYLOAD_OPEN_BOOK.equals(channel)) {
                SOpenBookWindowPacket openBookPacket = new SOpenBookWindowPacket();
                try {
                    openBookPacket.readPacketData(packet.getBufferData());
                } catch (IOException e) {
                    LOGGER.error("Failed to read open book packet", e);
                }
                handleOpenBookPacket(openBookPacket);
                ci.cancel();
            }
        }
    }

    @Inject(method = "handleJoinGame", at = @At("TAIL"))
    private void onOnGameJoin(SJoinGamePacket packet, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_13_2) {
            handleServerDifficulty(new SServerDifficultyPacket(PendingDifficulty.getPendingDifficulty(), false));
        }
    }

    @Inject(method = "handleRespawn", at = @At("TAIL"))
    private void onOnPlayerRespawn(SRespawnPacket packet, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_13_2) {
            handleServerDifficulty(new SServerDifficultyPacket(PendingDifficulty.getPendingDifficulty(), false));
        }
    }

    @Inject(method = "handleSpawnObject", at = @At("TAIL"))
    private void onOnEntitySpawn(SSpawnObjectPacket packet, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_13_2) {
            if (packet.getType() == EntityType.ITEM
                    || packet.getType() == EntityType.ARROW
                    || packet.getType() == EntityType.SPECTRAL_ARROW
                    || packet.getType() == EntityType.TRIDENT) {
                handleEntityVelocity(new SEntityVelocityPacket(packet.getEntityID(),
                        new Vec3d(packet.func_218693_g(), packet.func_218695_h(), packet.func_218692_i())));
            }
        }
    }

    @Inject(method = "handlePlayerPosLook", at = @At("TAIL"))
    private void onOnPlayerPositionLook(SPlayerPositionLookPacket packet, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_13_2) {
            Protocol_1_13_2.updateCameraPosition();
        }
    }

}
