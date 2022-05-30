package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.NetworkEnum;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.v1_12_2.CPacketCustomPayload_1_12_2;

@MessageVariant(minVersion = Protocols.V1_13)
public class CPacketUpdateStructureBlock {
    public CommonTypes.BlockPos pos;
    public Action action;
    public Mode mode;
    public String structureName;
    public byte offsetX;
    public byte offsetY;
    public byte offsetZ;
    public byte sizeX;
    public byte sizeY;
    public byte sizeZ;
    public Mirror mirror;
    public Rotation rotation;
    public String metadata;
    public float integrity;
    public long seed;
    public byte flags;

    @Handler(protocol = Protocols.V1_12_2)
    public static CPacketCustomPayload_1_12_2 toCustomPayload(
            @Argument("pos") CommonTypes.BlockPos pos,
            @Argument("action") Action action,
            @Argument("mode") Mode mode,
            @Argument("structureName") String structureName,
            @Argument("offsetX") byte offsetX,
            @Argument("offsetY") byte offsetY,
            @Argument("offsetZ") byte offsetZ,
            @Argument("sizeX") byte sizeX,
            @Argument("sizeY") byte sizeY,
            @Argument("sizeZ") byte sizeZ,
            @Argument("mirror") Mirror mirror,
            @Argument("rotation") Rotation rotation,
            @Argument("metadata") String metadata,
            @Argument("integrity") float integrity,
            @Argument("seed") long seed,
            @Argument("flags") byte flags
    ) {
        var packet = new CPacketCustomPayload_1_12_2.Struct();
        packet.channel = "MC|Struct";
        var mcPos = pos.toMinecraft();
        packet.x = mcPos.getX();
        packet.y = mcPos.getY();
        packet.z = mcPos.getZ();
        packet.action = switch (action) {
            case UPDATE -> 1;
            case SAVE -> 2;
            case LOAD -> 3;
            case SCAN -> 4;
        };
        packet.mode = mode.name();
        packet.structureName = structureName;
        packet.offsetX = offsetX;
        packet.offsetY = offsetY;
        packet.offsetZ = offsetZ;
        packet.sizeX = sizeX;
        packet.sizeY = sizeY;
        packet.sizeZ = sizeZ;
        packet.mirror = mirror.name();
        packet.rotation = rotation.name();
        packet.metadata = metadata;
        packet.ignoreEntities = (flags & 1) != 0;
        packet.showAir = (flags & 2) != 0;
        packet.showBoundingBox = (flags & 4) != 0;
        packet.integrity = integrity;
        packet.seed = seed;
        return packet;
    }

    @NetworkEnum
    public enum Action {
        UPDATE, SAVE, LOAD, SCAN
    }

    @NetworkEnum
    public enum Mode {
        SAVE, LOAD, CORNER, DATA
    }

    @NetworkEnum
    public enum Mirror {
        NONE, LEFT_RIGHT, FRONT_BACK
    }

    @NetworkEnum
    public enum Rotation {
        NONE, CLOCKWISE_90, CLOCKWISE_180, COUNTERCLOCKWISE_90
    }
}
