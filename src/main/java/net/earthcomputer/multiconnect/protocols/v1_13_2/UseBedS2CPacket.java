package net.earthcomputer.multiconnect.protocols.v1_13_2;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.Packet;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.BlockPos;

public class UseBedS2CPacket implements Packet<ClientPlayNetworkHandler> {

    private int playerId;
    private BlockPos bedPos;

    @Override
    public void read(PacketByteBuf buf) {
        playerId = buf.readVarInt();
        bedPos = buf.readBlockPos();
    }

    @Override
    public void write(PacketByteBuf buf) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void apply(ClientPlayNetworkHandler handler) {
        NetworkThreadUtils.forceMainThread(this, handler, MinecraftClient.getInstance());
        Entity entity = MinecraftClient.getInstance().world.getEntityById(playerId);
        if (entity instanceof PlayerEntity) {
            ((PlayerEntity) entity).trySleep(bedPos);
        }
    }
}
