package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CPacketRequestCommandCompletions;
import net.earthcomputer.multiconnect.packets.v1_12_2.CPacketRequestCommandCompletions_1_12_2;
import net.earthcomputer.multiconnect.packets.v1_13_2.BlockPos_1_13_2;
import net.earthcomputer.multiconnect.protocols.v1_12_2.TabCompletionManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

import java.util.Optional;

@MessageVariant(minVersion = Protocols.V1_13)
public class CPacketRequestCommandCompletions_Latest implements CPacketRequestCommandCompletions {
    public int transactionId;
    public String text;

    @Handler(protocol = Protocols.V1_12_2)
    public static CPacketRequestCommandCompletions_1_12_2 handle(
            @Argument("transactionId") int transactionId,
            @Argument("text") String text
    ) {
        TabCompletionManager.addTabCompletionRequest(transactionId, text);
        var packet = new CPacketRequestCommandCompletions_1_12_2();
        packet.command = text;
        // The server uses this to determine whether to look at the leading slash. We handle completion
        // inside command blocks ourselves so we set this to always be false for simplicity.
        packet.isFromCommandBlock = false;

        HitResult hitResult = MinecraftClient.getInstance().crosshairTarget;
        boolean hasTarget = hitResult != null && hitResult.getType() == HitResult.Type.BLOCK;
        if (hasTarget) {
            packet.target = Optional.of(BlockPos_1_13_2.fromMinecraft(((BlockHitResult) hitResult).getBlockPos()));
        } else {
            packet.target = Optional.empty();
        }
        return packet;
    }
}
