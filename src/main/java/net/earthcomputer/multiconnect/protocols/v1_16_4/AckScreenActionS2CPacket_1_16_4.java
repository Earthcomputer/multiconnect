package net.earthcomputer.multiconnect.protocols.v1_16_4;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.v1_11_2.IScreenHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.screen.ScreenHandler;

public class AckScreenActionS2CPacket_1_16_4 implements Packet<ClientPlayPacketListener> {
    private final int syncId;
    private final short actionId;
    private final boolean accepted;

    public AckScreenActionS2CPacket_1_16_4(PacketByteBuf buf) {
        syncId = buf.readUnsignedByte();
        actionId = buf.readShort();
        accepted = buf.readBoolean();
    }

    @Override
    public void write(PacketByteBuf buf) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void apply(ClientPlayPacketListener listener) {
        NetworkThreadUtils.forceMainThread(this, listener, MinecraftClient.getInstance());

        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null) {
            ScreenHandler screenHandler = null;
            if (syncId == 0) {
                screenHandler = player.playerScreenHandler;
            } else if (syncId == player.currentScreenHandler.syncId) {
                screenHandler = player.currentScreenHandler;
            }
            if (screenHandler != null) {
                if (ConnectionInfo.protocolVersion <= Protocols.V1_11_2) {
                    ((IScreenHandler) screenHandler).multiconnect_getRecipeBookEmulator().onAckScreenAction(this);
                }
                if (!accepted) {
                    player.networkHandler.sendPacket(new AckScreenActionC2SPacket_1_16_4(syncId, actionId, true));
                }
            }
        }
    }

    public short getActionId() {
        return actionId;
    }

    public boolean wasAccepted() {
        return accepted;
    }
}
