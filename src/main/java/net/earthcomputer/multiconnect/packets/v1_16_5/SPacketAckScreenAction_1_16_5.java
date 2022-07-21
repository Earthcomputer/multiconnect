package net.earthcomputer.multiconnect.packets.v1_16_5;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.DefaultConstruct;
import net.earthcomputer.multiconnect.ap.FilledArgument;
import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.impl.DelayedPacketSender;
import net.earthcomputer.multiconnect.protocols.v1_11.IContainerMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import java.util.function.Supplier;

@MessageVariant
public class SPacketAckScreenAction_1_16_5 {
    @Type(Types.UNSIGNED_BYTE)
    public int syncId;
    public short actionId;
    public boolean accepted;

    @Handler
    public static void handle(
            @Argument("syncId") int syncId,
            @Argument("actionId") short actionId,
            @Argument("accepted") boolean accepted,
            @DefaultConstruct Supplier<CPacketAckScreenAction_1_16_5> responsePacketCreator,
            @FilledArgument DelayedPacketSender<CPacketAckScreenAction_1_16_5> responseSender
    ) {
        Minecraft.getInstance().execute(() -> {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                AbstractContainerMenu screenHandler = null;
                if (syncId == 0) {
                    screenHandler = player.inventoryMenu;
                } else if (syncId == player.containerMenu.containerId) {
                    screenHandler = player.containerMenu;
                }
                if (screenHandler != null) {
                    if (ConnectionInfo.protocolVersion <= Protocols.V1_11_2) {
                        ((IContainerMenu) screenHandler).multiconnect_getRecipeBookEmulator().onAckScreenAction(actionId, accepted);
                    }
                    if (!accepted) {
                        CPacketAckScreenAction_1_16_5 response = responsePacketCreator.get();
                        response.syncId = syncId;
                        response.actionId = actionId;
                        response.accepted = true;
                        responseSender.send(response);
                    }
                }
            }
        });
    }
}
