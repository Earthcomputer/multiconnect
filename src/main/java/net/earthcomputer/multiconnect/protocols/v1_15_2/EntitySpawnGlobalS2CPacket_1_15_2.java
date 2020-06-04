package net.earthcomputer.multiconnect.protocols.v1_15_2;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;

public class EntitySpawnGlobalS2CPacket_1_15_2 implements Packet<ClientPlayPacketListener> {
    private double x;
    private double y;
    private double z;
    private int entityTypeId;

    @Override
    public void read(PacketByteBuf buf) {
        buf.readVarInt(); // id
        entityTypeId = buf.readByte();
        x = buf.readDouble();
        y = buf.readDouble();
        z = buf.readDouble();
    }

    @Override
    public void write(PacketByteBuf buf) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void apply(ClientPlayPacketListener listener) {
        NetworkThreadUtils.forceMainThread(this, listener, MinecraftClient.getInstance());
        if (entityTypeId == 1) {
            LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(MinecraftClient.getInstance().world);
            if (lightning == null) {
                return;
            }
            lightning.setPos(x, y, z);
            listener.onEntitySpawn(new EntitySpawnS2CPacket(lightning));
        }
    }
}
