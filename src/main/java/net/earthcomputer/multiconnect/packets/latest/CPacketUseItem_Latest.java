package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CPacketUseItem;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.v1_18_2.CPacketUseItem_1_18_2;
import net.earthcomputer.multiconnect.protocols.v1_18.IBlockStatePredictionHandler;
import net.earthcomputer.multiconnect.protocols.v1_18.mixin.ClientLevelAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

@MessageVariant(minVersion = Protocols.V1_19)
public class CPacketUseItem_Latest implements CPacketUseItem {
    public CommonTypes.Hand hand;
    public int sequence;

    @SuppressWarnings("resource")
    @Handler(protocol = Protocols.V1_18_2)
    public static CPacketUseItem_1_18_2 handle(
            @Argument(value = "this", translate = true) CPacketUseItem_1_18_2 translatedThis,
            @Argument("sequence") int sequence
    ) {
        Minecraft.getInstance().execute(() -> {
            ClientLevel level = Minecraft.getInstance().level;
            if (level != null) {
                var blockStatePredictionHandler = (IBlockStatePredictionHandler) ((ClientLevelAccessor) level).multiconnect_getBlockStatePredictionHandler();
                blockStatePredictionHandler.multiconnect_nullifyServerVerifiedStatesUpTo(level, sequence);
                level.handleBlockChangedAck(sequence);
            }
        });
        return translatedThis;
    }
}
