package net.earthcomputer.multiconnect.packets.v1_16_5;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.DefaultConstruct;
import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.NetworkEnum;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketSetBorderCenter;
import net.earthcomputer.multiconnect.packets.SPacketInitializeBorder;
import net.earthcomputer.multiconnect.packets.SPacketSetBorderLerpSize;
import net.earthcomputer.multiconnect.packets.SPacketSetBorderSize;
import net.earthcomputer.multiconnect.packets.SPacketSetBorderWarningDistance;
import net.earthcomputer.multiconnect.packets.SPacketSetBorderWarningDelay;

@MessageVariant(maxVersion = Protocols.V1_16_5)
@Polymorphic
public abstract class SPacketWorldBorder_1_16_5 {
    public Mode mode;

    @Polymorphic(stringValue = "SET_SIZE")
    @MessageVariant(maxVersion = Protocols.V1_16_5)
    public static class SetSize extends SPacketWorldBorder_1_16_5 {
        public double newSize;

        @Handler
        public static SPacketSetBorderSize handle(
                @Argument("newSize") double newSize,
                @DefaultConstruct SPacketSetBorderSize packet
        ) {
            packet.diameter = newSize;
            return packet;
        }
    }

    @Polymorphic(stringValue = "LERP_SIZE")
    @MessageVariant(maxVersion = Protocols.V1_16_5)
    public static class LerpSize extends SPacketWorldBorder_1_16_5 {
        public double oldSize;
        public double newSize;
        public long lerpTime;

        @Handler
        public static SPacketSetBorderLerpSize handle(
                @Argument("oldSize") double oldSize,
                @Argument("newSize") double newSize,
                @Argument("lerpTime") long lerpTime,
                @DefaultConstruct SPacketSetBorderLerpSize packet
        ) {
            packet.oldDiameter = oldSize;
            packet.newDiameter = newSize;
            packet.time = lerpTime;
            return packet;
        }
    }

    @Polymorphic(stringValue = "SET_CENTER")
    @MessageVariant(maxVersion = Protocols.V1_16_5)
    public static class SetCenter extends SPacketWorldBorder_1_16_5 {
        public double x;
        public double z;

        @Handler
        public static SPacketSetBorderCenter handle(
                @Argument("x") double x,
                @Argument("z") double z,
                @DefaultConstruct SPacketSetBorderCenter packet
        ) {
            packet.x = x;
            packet.z = z;
            return packet;
        }
    }

    @Polymorphic(stringValue = "SET_WARNING_BLOCKS")
    @MessageVariant(maxVersion = Protocols.V1_16_5)
    public static class SetWarningBlocks extends SPacketWorldBorder_1_16_5 {
        public int warningBlocks;

        @Handler
        public static SPacketSetBorderWarningDistance handle(
                @Argument("warningBlocks") int warningBlocks,
                @DefaultConstruct SPacketSetBorderWarningDistance packet
        ) {
            packet.warningBlocks = warningBlocks;
            return packet;
        }
    }

    @Polymorphic(stringValue = "SET_WARNING_TIME")
    @MessageVariant(maxVersion = Protocols.V1_16_5)
    public static class SetWarningTime extends SPacketWorldBorder_1_16_5 {
        public int warningTime;

        @Handler
        public static SPacketSetBorderWarningDelay handle(
                @Argument("warningTime") int warningTime,
                @DefaultConstruct SPacketSetBorderWarningDelay packet
        ) {
            packet.warningTime = warningTime;
            return packet;
        }
    }

    @Polymorphic(stringValue = "INITIALIZE")
    @MessageVariant(maxVersion = Protocols.V1_16_5)
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
        public static SPacketInitializeBorder handle(
                @Argument("x") double x,
                @Argument("z") double z,
                @Argument("oldSize") double oldSize,
                @Argument("newSize") double newSize,
                @Argument("lerpTime") long lerpTime,
                @Argument("newAbsoluteMaxSize") int newAbsoluteMaxSize,
                @Argument("warningBlocks") int warningBlocks,
                @Argument("warningTime") int warningTime,
                @DefaultConstruct SPacketInitializeBorder packet
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

    @NetworkEnum
    public enum Mode {
        SET_SIZE, LERP_SIZE, SET_CENTER, INITIALIZE, SET_WARNING_TIME, SET_WARNING_BLOCKS
    }
}
