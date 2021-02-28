package net.earthcomputer.multiconnect.protocols.v1_16_4;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.transformer.TransformerByteBuf;
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
                TransformerByteBuf buf = TransformerByteBuf.forPacketConstruction(class_5897.class, Protocols.V1_17);
                buf.pendingRead(Double.class, newSize);
                buf.applyPendingReads();
                listener.method_34079(new class_5897(buf));
            }
            break;
            case LERP_SIZE: {
                TransformerByteBuf buf = TransformerByteBuf.forPacketConstruction(class_5896.class, Protocols.V1_17);
                buf.pendingRead(Double.class, oldSize);
                buf.pendingRead(Double.class, newSize);
                buf.pendingRead(VarLong.class, new VarLong(lerpTime));
                buf.applyPendingReads();
                listener.method_34078(new class_5896(buf));
            }
            break;
            case SET_CENTER: {
                TransformerByteBuf buf = TransformerByteBuf.forPacketConstruction(class_5895.class, Protocols.V1_17);
                buf.pendingRead(Double.class, newCenterX);
                buf.pendingRead(Double.class, newCenterZ);
                buf.applyPendingReads();
                listener.method_34077(new class_5895(buf));
            }
            break;
            case SET_WARNING_BLOCKS: {
                TransformerByteBuf buf = TransformerByteBuf.forPacketConstruction(class_5899.class, Protocols.V1_17);
                buf.pendingRead(VarInt.class, new VarInt(warningBlocks));
                buf.applyPendingReads();
                listener.method_34081(new class_5899(buf));
            }
            break;
            case SET_WARNING_TIME: {
                TransformerByteBuf buf = TransformerByteBuf.forPacketConstruction(class_5898.class, Protocols.V1_17);
                buf.pendingRead(VarInt.class, new VarInt(warningTime));
                buf.applyPendingReads();
                listener.method_34080(new class_5898(buf));
            }
            break;
            case INITIALIZE: {
                TransformerByteBuf buf = TransformerByteBuf.forPacketConstruction(class_5889.class, Protocols.V1_17);
                buf.pendingRead(Double.class, newCenterX);
                buf.pendingRead(Double.class, newCenterZ);
                buf.pendingRead(Double.class, oldSize);
                buf.pendingRead(Double.class, newSize);
                buf.pendingRead(VarLong.class, new VarLong(lerpTime));
                buf.pendingRead(VarInt.class, new VarInt(newAbsoluteMaxSize));
                buf.pendingRead(VarInt.class, new VarInt(warningBlocks));
                buf.pendingRead(VarInt.class, new VarInt(warningTime));
                buf.applyPendingReads();
                listener.method_34072(new class_5889(buf));
            }
            break;
        }
    }

    private enum Type {
        SET_SIZE, LERP_SIZE, SET_CENTER, INITIALIZE, SET_WARNING_TIME, SET_WARNING_BLOCKS
    }
}
