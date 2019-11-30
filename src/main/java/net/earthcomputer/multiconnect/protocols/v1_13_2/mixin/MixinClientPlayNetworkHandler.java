package net.earthcomputer.multiconnect.protocols.v1_13_2.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.v1_13_2.ILightUpdatePacket;
import net.earthcomputer.multiconnect.protocols.v1_13_2.PendingDifficulty;
import net.earthcomputer.multiconnect.protocols.v1_13_2.PendingLightData;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.packet.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler {

    private static final Identifier CUSTOM_PAYLOAD_TRADE_LIST = new Identifier("trade_list");
    private static final Identifier CUSTOM_PAYLOAD_OPEN_BOOK = new Identifier("open_book");

    @Shadow @Final private static Logger LOGGER;
    @Shadow private ClientWorld world;

    @Shadow public abstract void onLightUpdate(LightUpdateS2CPacket packet);

    @Shadow public abstract void onOpenWrittenBook(OpenWrittenBookS2CPacket packet);

    @Shadow public abstract void onDifficulty(DifficultyS2CPacket difficultyS2CPacket_1);

    @Inject(method = "onChunkData", at = @At("TAIL"))
    private void onChunkDataPost(ChunkDataS2CPacket packet, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_13_2) {
            LightUpdateS2CPacket lightPacket = new LightUpdateS2CPacket();
            @SuppressWarnings("ConstantConditions") ILightUpdatePacket iLightPacket = (ILightUpdatePacket) lightPacket;

            PendingLightData lightData = PendingLightData.getInstance();

            iLightPacket.setChunkX(packet.getX());
            iLightPacket.setChunkZ(packet.getZ());

            int blockLightMask = (packet.getVerticalStripBitmask() & 0xffff) << 1;
            int skyLightMask = packet.isFullChunk() ? 0x3ffff : world.dimension.hasSkyLight() ? blockLightMask : 0;
            iLightPacket.setBlocklightMask(blockLightMask);
            iLightPacket.setSkylightMask(skyLightMask);

            for (int i = 0; i < 16; i++) {
                byte[] blockData = lightData.getBlockLight(i);
                if (blockData != null)
                    lightPacket.getBlockLightUpdates().add(blockData);
                byte[] skyData = lightData.getSkyLight(i);
                if (skyData != null)
                    lightPacket.getSkyLightUpdates().add(skyData);
            }

            PendingLightData.setInstance(null);

            onLightUpdate(lightPacket);
        }
    }

    @Inject(method = "onCustomPayload", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/packet/CustomPayloadS2CPacket;getChannel()Lnet/minecraft/util/Identifier;"), cancellable = true)
    private void onOnCustomPayload(CustomPayloadS2CPacket packet, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_13_2) {
            Identifier channel = packet.getChannel();
            if (CUSTOM_PAYLOAD_TRADE_LIST.equals(channel)) {
                // TODO: trading
                ci.cancel();
            } else if (CUSTOM_PAYLOAD_OPEN_BOOK.equals(channel)) {
                OpenWrittenBookS2CPacket openBookPacket = new OpenWrittenBookS2CPacket();
                try {
                    openBookPacket.read(packet.getData());
                } catch (IOException e) {
                    LOGGER.error("Failed to read open book packet", e);
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

}
