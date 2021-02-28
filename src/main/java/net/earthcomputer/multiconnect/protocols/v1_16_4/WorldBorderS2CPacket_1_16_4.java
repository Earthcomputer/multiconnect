package net.earthcomputer.multiconnect.protocols.v1_16_4;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.Utils;
import net.earthcomputer.multiconnect.transformer.VarInt;
import net.earthcomputer.multiconnect.transformer.VarLong;
import net.minecraft.*;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;

public class WorldBorderS2CPacket_1_16_4 implements Packet<ClientPlayPacketListener> {
    private final Type type;
    private int newAbsoluteMaxSize;
    private double newCenterX;
    private double newCenterZ;
    private double newSize;
    private double oldSize;
    private long lerpTime;
    private int warningTime;
    private int warningBlocks;

    public WorldBorderS2CPacket_1_16_4(PacketByteBuf buf) {
        this.type = buf.readEnumConstant(Type.class);
        switch (type) {
            case SET_SIZE:
                this.newSize = buf.readDouble();
                break;
            case LERP_SIZE:
                this.oldSize = buf.readDouble();
                this.newSize = buf.readDouble();
                this.lerpTime = buf.readVarLong();
                break;
            case SET_CENTER:
                this.newCenterX = buf.readDouble();
                this.newCenterZ = buf.readDouble();
                break;
            case SET_WARNING_BLOCKS:
                this.warningBlocks = buf.readVarInt();
                break;
            case SET_WARNING_TIME:
                this.warningTime = buf.readVarInt();
                break;
            case INITIALIZE:
                this.newCenterX = buf.readDouble();
                this.newCenterZ = buf.readDouble();
                this.oldSize = buf.readDouble();
                this.newSize = buf.readDouble();
                this.lerpTime = buf.readVarLong();
                this.newAbsoluteMaxSize = buf.readVarInt();
                this.warningBlocks = buf.readVarInt();
                this.warningTime = buf.readVarInt();
                break;
        }
    }

    @Override
    public void write(PacketByteBuf buf) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void apply(ClientPlayPacketListener listener) {
        switch (type) {
            case SET_SIZE: {
                class_5897 packet = Utils.createPacket(class_5897.class, class_5897::new, Protocols.V1_17, buf -> {
                    buf.pendingRead(Double.class, newSize);
                    buf.applyPendingReads();
                });
                listener.method_34079(packet);
            }
            break;
            case LERP_SIZE: {
                class_5896 packet = Utils.createPacket(class_5896.class, class_5896::new, Protocols.V1_17, buf -> {
                    buf.pendingRead(Double.class, oldSize);
                    buf.pendingRead(Double.class, newSize);
                    buf.pendingRead(VarLong.class, new VarLong(lerpTime));
                    buf.applyPendingReads();
                });
                listener.method_34078(packet);
            }
            break;
            case SET_CENTER: {
                class_5895 packet = Utils.createPacket(class_5895.class, class_5895::new, Protocols.V1_17, buf -> {
                    buf.pendingRead(Double.class, newCenterX);
                    buf.pendingRead(Double.class, newCenterZ);
                    buf.applyPendingReads();
                });
                listener.method_34077(packet);
            }
            break;
            case SET_WARNING_BLOCKS: {
                class_5899 packet = Utils.createPacket(class_5899.class, class_5899::new, Protocols.V1_17, buf -> {
                    buf.pendingRead(VarInt.class, new VarInt(warningBlocks));
                    buf.applyPendingReads();
                });
                listener.method_34081(packet);
            }
            break;
            case SET_WARNING_TIME: {
                class_5898 packet = Utils.createPacket(class_5898.class, class_5898::new, Protocols.V1_17, buf -> {
                    buf.pendingRead(VarInt.class, new VarInt(warningTime));
                    buf.applyPendingReads();
                });
                listener.method_34080(packet);
            }
            break;
            case INITIALIZE: {
                class_5889 packet = Utils.createPacket(class_5889.class, class_5889::new, Protocols.V1_17, buf -> {
                    buf.pendingRead(Double.class, newCenterX);
                    buf.pendingRead(Double.class, newCenterZ);
                    buf.pendingRead(Double.class, oldSize);
                    buf.pendingRead(Double.class, newSize);
                    buf.pendingRead(VarLong.class, new VarLong(lerpTime));
                    buf.pendingRead(VarInt.class, new VarInt(newAbsoluteMaxSize));
                    buf.pendingRead(VarInt.class, new VarInt(warningBlocks));
                    buf.pendingRead(VarInt.class, new VarInt(warningTime));
                    buf.applyPendingReads();
                });
                listener.method_34072(packet);
            }
            break;
        }
    }

    private enum Type {
        SET_SIZE, LERP_SIZE, SET_CENTER, INITIALIZE, SET_WARNING_TIME, SET_WARNING_BLOCKS
    }
}
