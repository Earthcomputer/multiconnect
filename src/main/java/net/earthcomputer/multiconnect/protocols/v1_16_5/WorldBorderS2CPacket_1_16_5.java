package net.earthcomputer.multiconnect.protocols.v1_16_5;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.Utils;
import net.earthcomputer.multiconnect.transformer.VarInt;
import net.earthcomputer.multiconnect.transformer.VarLong;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.*;

public class WorldBorderS2CPacket_1_16_5 implements Packet<ClientPlayPacketListener> {
    private final Type type;
    private int newAbsoluteMaxSize;
    private double newCenterX;
    private double newCenterZ;
    private double newSize;
    private double oldSize;
    private long lerpTime;
    private int warningTime;
    private int warningBlocks;

    public WorldBorderS2CPacket_1_16_5(PacketByteBuf buf) {
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
                WorldBorderSizeChangedS2CPacket packet = Utils.createPacket(WorldBorderSizeChangedS2CPacket.class, WorldBorderSizeChangedS2CPacket::new, Protocols.V1_17, buf -> {
                    buf.pendingRead(Double.class, newSize);
                    buf.applyPendingReads();
                });
                listener.onWorldBorderSizeChanged(packet);
            }
            break;
            case LERP_SIZE: {
                WorldBorderInterpolateSizeS2CPacket packet = Utils.createPacket(WorldBorderInterpolateSizeS2CPacket.class, WorldBorderInterpolateSizeS2CPacket::new, Protocols.V1_17, buf -> {
                    buf.pendingRead(Double.class, oldSize);
                    buf.pendingRead(Double.class, newSize);
                    buf.pendingRead(VarLong.class, new VarLong(lerpTime));
                    buf.applyPendingReads();
                });
                listener.onWorldBorderInterpolateSize(packet);
            }
            break;
            case SET_CENTER: {
                WorldBorderCenterChangedS2CPacket packet = Utils.createPacket(WorldBorderCenterChangedS2CPacket.class, WorldBorderCenterChangedS2CPacket::new, Protocols.V1_17, buf -> {
                    buf.pendingRead(Double.class, newCenterX);
                    buf.pendingRead(Double.class, newCenterZ);
                    buf.applyPendingReads();
                });
                listener.onWorldBorderCenterChanged(packet);
            }
            break;
            case SET_WARNING_BLOCKS: {
                WorldBorderWarningBlocksChangedS2CPacket packet = Utils.createPacket(WorldBorderWarningBlocksChangedS2CPacket.class, WorldBorderWarningBlocksChangedS2CPacket::new, Protocols.V1_17, buf -> {
                    buf.pendingRead(VarInt.class, new VarInt(warningBlocks));
                    buf.applyPendingReads();
                });
                listener.onWorldBorderWarningBlocksChanged(packet);
            }
            break;
            case SET_WARNING_TIME: {
                WorldBorderWarningTimeChangedS2CPacket packet = Utils.createPacket(WorldBorderWarningTimeChangedS2CPacket.class, WorldBorderWarningTimeChangedS2CPacket::new, Protocols.V1_17, buf -> {
                    buf.pendingRead(VarInt.class, new VarInt(warningTime));
                    buf.applyPendingReads();
                });
                listener.onWorldBorderWarningTimeChanged(packet);
            }
            break;
            case INITIALIZE: {
                WorldBorderInitializeS2CPacket packet = Utils.createPacket(WorldBorderInitializeS2CPacket.class, WorldBorderInitializeS2CPacket::new, Protocols.V1_17, buf -> {
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
                listener.onWorldBorderInitialize(packet);
            }
            break;
        }
    }

    private enum Type {
        SET_SIZE, LERP_SIZE, SET_CENTER, INITIALIZE, SET_WARNING_TIME, SET_WARNING_BLOCKS
    }
}
