package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;

@Message
public class CPacketUpdateStructureBlock {
    public CommonTypes.BlockPos pos;
    public Action action;
    public Mode mode;
    public String name;
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

    public enum Action {
        UPDATE, SAVE, LOAD, SCAN
    }

    public enum Mode {
        SAVE, LOAD, CORNER, DATA
    }

    public enum Mirror {
        NONE, LEFT_RIGHT, FRONT_BACK
    }

    public enum Rotation {
        NONE, CLOCKWISE_90, CLOCKWISE_180, COUNTERCLOCKWISE_90
    }
}
