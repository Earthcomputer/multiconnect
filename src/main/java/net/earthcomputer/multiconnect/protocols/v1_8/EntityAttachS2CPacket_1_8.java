package net.earthcomputer.multiconnect.protocols.v1_8;

import net.earthcomputer.multiconnect.protocols.v1_8.mixin.EntityAttachS2CAccessor;
import net.earthcomputer.multiconnect.protocols.v1_8.mixin.EntityPassengersSetS2CAccessor;
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

    private int fromEntityId;
    private int toEntityId;
    private int attachType;

    @Override
    public void read(PacketByteBuf buf) {
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
            EntityPassengersSetS2CPacket packet = new EntityPassengersSetS2CPacket();
            //noinspection ConstantConditions
            EntityPassengersSetS2CAccessor accessor = (EntityPassengersSetS2CAccessor) packet;

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
                accessor.setId(vehicle.getId());
                accessor.setPassengerIds(new int[0]);
            } else {
                accessor.setId(toEntityId);
                accessor.setPassengerIds(new int[] {fromEntityId});
            }

            listener.onEntityPassengersSet(packet);
        } else if (attachType == 1) { // leash
            EntityAttachS2CPacket packet = new EntityAttachS2CPacket();
            //noinspection ConstantConditions
            EntityAttachS2CAccessor accessor = (EntityAttachS2CAccessor) packet;
            accessor.setHoldingId(toEntityId);
            accessor.setAttachedId(fromEntityId);
            listener.onEntityAttach(packet);
        }
    }
}
