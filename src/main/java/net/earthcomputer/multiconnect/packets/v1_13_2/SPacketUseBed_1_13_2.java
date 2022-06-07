package net.earthcomputer.multiconnect.packets.v1_13_2;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

@MessageVariant(maxVersion = Protocols.V1_13_2)
public class SPacketUseBed_1_13_2 {
    public int playerId;
    public CommonTypes.BlockPos bedPos;

    @Handler
    public static void handle(
            @Argument("playerId") int playerId,
            @Argument("bedPos") CommonTypes.BlockPos bedPos
    ) {
        // TODO: convert this to a packet?
        MinecraftClient.getInstance().execute(() -> {
            ClientWorld world = MinecraftClient.getInstance().world;
            if (world != null) {
                Entity entity = world.getEntityById(playerId);
                if (entity instanceof PlayerEntity player) {
                    player.trySleep(bedPos.toMinecraft());
                }
            }
        });
    }
}
