package net.earthcomputer.multiconnect.protocols.v1_8;

import net.earthcomputer.multiconnect.protocols.v1_8.mixin.EntityAttachS2CAccessor;
import net.earthcomputer.multiconnect.protocols.v1_8.mixin.EntityPassengersSetS2CAccessor;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.EntityAttachS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;

public class EntityAttachS2CPacket_1_8 implements Packet<ClientPlayPacketListener> {
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
        if (attachType == 0) { // mount
            EntityPassengersSetS2CPacket packet = new EntityPassengersSetS2CPacket();
            //noinspection ConstantConditions
            EntityPassengersSetS2CAccessor accessor = (EntityPassengersSetS2CAccessor) packet;
            accessor.setId(toEntityId);
            accessor.setPassengerIds(new int[] {fromEntityId});
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
