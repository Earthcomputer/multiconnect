package net.earthcomputer.multiconnect.packets.v1_16_5;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.DefaultConstruct;
import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.earthcomputer.multiconnect.packets.SPacketWorldBorderCenterChanged;
import net.earthcomputer.multiconnect.packets.SPacketWorldBorderInitialize;
import net.earthcomputer.multiconnect.packets.SPacketWorldBorderInterpolateSize;
import net.earthcomputer.multiconnect.packets.SPacketWorldBorderSizeChanged;
import net.earthcomputer.multiconnect.packets.SPacketWorldBorderWarningBlocksChanged;
import net.earthcomputer.multiconnect.packets.SPacketWorldBorderWarningTimeChanged;

@Message
@Polymorphic
public abstract class SPacketWorldBorder_1_16_5 {
    public Mode mode;

    @Polymorphic(stringValue = "SET_SIZE")
    @Message
    public static class SetSize extends SPacketWorldBorder_1_16_5 {
        public double newSize;

        @Handler
        public static SPacketWorldBorderSizeChanged handle(
                @Argument("newSize") double newSize,
                @DefaultConstruct SPacketWorldBorderSizeChanged packet
        ) {
            packet.diameter = newSize;
            return packet;
        }
    }

    @Polymorphic(stringValue = "LERP_SIZE")
    @Message
    public static class LerpSize extends SPacketWorldBorder_1_16_5 {
        public double oldSize;
        public double newSize;
        public long lerpTime;

        @Handler
        public static SPacketWorldBorderInterpolateSize handle(
                @Argument("oldSize") double oldSize,
                @Argument("newSize") double newSize,
                @Argument("lerpTime") long lerpTime,
                @DefaultConstruct SPacketWorldBorderInterpolateSize packet
        ) {
            packet.oldDiameter = oldSize;
            packet.newDiameter = newSize;
            packet.time = lerpTime;
            return packet;
        }
    }

    @Polymorphic(stringValue = "SET_CENTER")
    @Message
    public static class SetCenter extends SPacketWorldBorder_1_16_5 {
        public double x;
        public double z;

        @Handler
        public static SPacketWorldBorderCenterChanged handle(
                @Argument("x") double x,
                @Argument("z") double z,
                @DefaultConstruct SPacketWorldBorderCenterChanged packet
        ) {
            packet.x = x;
            packet.z = z;
            return packet;
        }
    }

    @Polymorphic(stringValue = "SET_WARNING_BLOCKS")
    @Message
    public static class SetWarningBlocks extends SPacketWorldBorder_1_16_5 {
        public int warningBlocks;

        @Handler
        public static SPacketWorldBorderWarningBlocksChanged handle(
                @Argument("warningBlocks") int warningBlocks,
                @DefaultConstruct SPacketWorldBorderWarningBlocksChanged packet
        ) {
            packet.warningBlocks = warningBlocks;
            return packet;
        }
    }

    @Polymorphic(stringValue = "SET_WARNING_TIME")
    @Message
    public static class SetWarningTime extends SPacketWorldBorder_1_16_5 {
        public int warningTime;

        @Handler
        public static SPacketWorldBorderWarningTimeChanged handle(
                @Argument("warningTime") int warningTime,
                @DefaultConstruct SPacketWorldBorderWarningTimeChanged packet
        ) {
            packet.warningTime = warningTime;
            return packet;
        }
    }

    @Polymorphic(stringValue = "INITIALIZE")
    @Message
    public static class Initialize extends SPacketWorldBorder_1_16_5 {
        public double x;
        public double z;
        public double oldSize;
        public double newSize;
        public long lerpTime;
        public int newAbsoluteMaxSize;
        public int warningBlocks;
        public int warningTime;

        @Handler
        public static SPacketWorldBorderInitialize handle(
                @Argument("x") double x,
                @Argument("z") double z,
                @Argument("oldSize") double oldSize,
                @Argument("newSize") double newSize,
                @Argument("lerpTime") long lerpTime,
                @Argument("newAbsoluteMaxSize") int newAbsoluteMaxSize,
                @Argument("warningBlocks") int warningBlocks,
                @Argument("warningTime") int warningTime,
                @DefaultConstruct SPacketWorldBorderInitialize packet
        ) {
            packet.x = x;
            packet.z = z;
            packet.size = oldSize;
            packet.sizeLerpTarget = newSize;
            packet.sizeLerpTime = lerpTime;
            packet.maxRadius = newAbsoluteMaxSize;
            packet.warningBlocks = warningBlocks;
            packet.warningTime = warningTime;
            return packet;
        }
    }

    public enum Mode {
        SET_SIZE, LERP_SIZE, SET_CENTER, INITIALIZE, SET_WARNING_TIME, SET_WARNING_BLOCKS
    }
}
