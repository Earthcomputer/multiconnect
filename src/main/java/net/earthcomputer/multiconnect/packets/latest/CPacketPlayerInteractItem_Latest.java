package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CPacketPlayerInteractItem;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.v1_18_2.CPacketPlayerInteractItem_1_18_2;
import net.earthcomputer.multiconnect.protocols.v1_18.IBlockStatePredictionHandler;
import net.earthcomputer.multiconnect.protocols.v1_18.mixin.ClientLevelAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

@MessageVariant(minVersion = Protocols.V1_19)
public class CPacketPlayerInteractItem_Latest implements CPacketPlayerInteractItem {
    public CommonTypes.Hand hand;
    public int sequence;

    @SuppressWarnings("resource")
    @Handler(protocol = Protocols.V1_18_2)
    public static CPacketPlayerInteractItem_1_18_2 handle(
            @Argument(value = "this", translate = true) CPacketPlayerInteractItem_1_18_2 translatedThis,
            @Argument("sequence") int sequence
    ) {
        Minecraft.getInstance().execute(() -> {
            ClientLevel world = Minecraft.getInstance().level;
            if (world != null) {
                var pendingUpdateManager = (IBlockStatePredictionHandler) ((ClientLevelAccessor) world).multiconnect_getBlockStatePredictionHandler();
                pendingUpdateManager.multiconnect_nullifyServerVerifiedStatesUpTo(world, sequence);
                world.handleBlockChangedAck(sequence);
            }
        });
        return translatedThis;
    }
}
