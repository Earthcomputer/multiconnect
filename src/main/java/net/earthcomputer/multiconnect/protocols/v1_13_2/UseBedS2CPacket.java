package net.earthcomputer.multiconnect.protocols.v1_13_2;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.util.math.BlockPos;

public class UseBedS2CPacket implements IPacket<ClientPlayNetHandler> {

    private int playerId;
    private BlockPos bedPos;

    @Override
    public void readPacketData(PacketBuffer buf) {
        playerId = buf.readVarInt();
        bedPos = buf.readBlockPos();
    }

    @Override
    public void writePacketData(PacketBuffer buf) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void processPacket(ClientPlayNetHandler handler) {
        PacketThreadUtil.checkThreadAndEnqueue(this, handler, Minecraft.getInstance());
        Entity entity = Minecraft.getInstance().world.getEntityByID(playerId);
        if (entity instanceof PlayerEntity) {
            ((PlayerEntity) entity).trySleep(bedPos);
        }
    }
}
