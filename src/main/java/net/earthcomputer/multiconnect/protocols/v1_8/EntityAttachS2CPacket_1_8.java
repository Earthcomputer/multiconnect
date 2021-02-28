package net.earthcomputer.multiconnect.protocols.v1_8;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.Utils;
import net.earthcomputer.multiconnect.transformer.VarInt;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.EntityAttachS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EntityAttachS2CPacket_1_8 implements Packet<ClientPlayPacketListener> {
    private static final Logger LOGGER = LogManager.getLogger("multiconnect");

    private final int fromEntityId;
    private final int toEntityId;
    private final int attachType;

    public EntityAttachS2CPacket_1_8(PacketByteBuf buf) {
        fromEntityId = buf.readInt();
        toEntityId = buf.readInt();
        attachType = buf.readUnsignedByte();
    }

    @Override
    public void write(PacketByteBuf buf) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void apply(ClientPlayPacketListener listener) {
        NetworkThreadUtils.forceMainThread(this, listener, MinecraftClient.getInstance());

        if (attachType == 0) { // mount
            int vehicleId;
            int[] passengerIds;

            if (toEntityId == -1) {
                // dismount
                ClientWorld world = MinecraftClient.getInstance().world;
                if (world == null) {
                    return;
                }
                Entity passenger = world.getEntityById(fromEntityId);
                if (passenger == null) {
                    LOGGER.warn("Received dismount for unknown entity");
                    return;
                }
                Entity vehicle = passenger.getVehicle();
                if (vehicle == null) {
                    LOGGER.warn("Received dismount for non-riding entity");
                    return;
                }
                vehicleId = vehicle.getId();
                passengerIds = new int[0];
            } else {
                vehicleId = toEntityId;
                passengerIds = new int[] {fromEntityId};
            }

            EntityPassengersSetS2CPacket packet = Utils.createPacket(EntityPassengersSetS2CPacket.class, EntityPassengersSetS2CPacket::new, Protocols.V1_9, buf -> {
                buf.pendingRead(VarInt.class, new VarInt(vehicleId));
                buf.pendingRead(int[].class, passengerIds);
                buf.applyPendingReads();
            });

            listener.onEntityPassengersSet(packet);
        } else if (attachType == 1) { // leash
            EntityAttachS2CPacket packet = Utils.createPacket(EntityAttachS2CPacket.class, EntityAttachS2CPacket::new, Protocols.V1_9, buf -> {
                buf.pendingRead(Integer.class, fromEntityId); // attached id
                buf.pendingRead(Integer.class, toEntityId); // holding id
                buf.applyPendingReads();
            });
            listener.onEntityAttach(packet);
        }
    }
}
