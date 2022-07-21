package net.earthcomputer.multiconnect.packets.v1_13_2;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

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
        Minecraft.getInstance().execute(() -> {
            ClientLevel world = Minecraft.getInstance().level;
            if (world != null) {
                Entity entity = world.getEntity(playerId);
                if (entity instanceof Player player) {
                    player.startSleepInBed(bedPos.toMinecraft());
                }
            }
        });
    }
}
