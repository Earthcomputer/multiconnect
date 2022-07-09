package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Sendable;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CPacketCommandSuggestion;
import net.earthcomputer.multiconnect.packets.v1_12_2.CPacketCommandSuggestion_1_12_2;
import net.earthcomputer.multiconnect.packets.v1_13_2.BlockPos_1_13_2;
import net.earthcomputer.multiconnect.protocols.v1_12.TabCompletionManager;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import java.util.Optional;

@MessageVariant(minVersion = Protocols.V1_13)
@Sendable(from = Protocols.V1_13)
public class CPacketCommandSuggestion_Latest implements CPacketCommandSuggestion {
    public int transactionId;
    public String text;

    @Handler(protocol = Protocols.V1_12_2)
    public static CPacketCommandSuggestion_1_12_2 handle(
            @Argument("transactionId") int transactionId,
            @Argument("text") String text
    ) {
        TabCompletionManager.addTabCompletionRequest(transactionId, text);
        var packet = new CPacketCommandSuggestion_1_12_2();
        packet.command = text;
        // The server uses this to determine whether to look at the leading slash. We handle completion
        // inside command blocks ourselves so we set this to always be false for simplicity.
        packet.isFromCommandBlock = false;

        HitResult hitResult = Minecraft.getInstance().hitResult;
        boolean hasTarget = hitResult != null && hitResult.getType() == HitResult.Type.BLOCK;
        if (hasTarget) {
            packet.target = Optional.of(BlockPos_1_13_2.fromMinecraft(((BlockHitResult) hitResult).getBlockPos()));
        } else {
            packet.target = Optional.empty();
        }
        return packet;
    }
}
