package net.earthcomputer.multiconnect.packets.v1_16_5;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.DefaultConstruct;
import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.NetworkEnum;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketClearTitles;
import net.earthcomputer.multiconnect.packets.SPacketSetActionBarText;
import net.earthcomputer.multiconnect.packets.SPacketSetSubtitleText;
import net.earthcomputer.multiconnect.packets.SPacketSetTitleText;
import net.earthcomputer.multiconnect.packets.SPacketSetTitlesAnimation;

@MessageVariant(maxVersion = Protocols.V1_16_5)
@Polymorphic
public abstract class SPacketTitle_1_16_5 {
    public Mode mode;

    @Polymorphic(stringValue = "TITLE")
    @MessageVariant(maxVersion = Protocols.V1_16_5)
    public static class Title extends SPacketTitle_1_16_5 {
        public CommonTypes.Text text;

        @Handler
        public static SPacketSetTitleText handle(
                @Argument("text") CommonTypes.Text text,
                @DefaultConstruct SPacketSetTitleText packet
        ) {
            packet.title = text;
            return packet;
        }
    }

    @Polymorphic(stringValue = "SUBTITLE")
    @MessageVariant(maxVersion = Protocols.V1_16_5)
    public static class Subtitle extends SPacketTitle_1_16_5 {
        public CommonTypes.Text text;

        @Handler
        public static SPacketSetSubtitleText handle(
                @Argument("text") CommonTypes.Text text,
                @DefaultConstruct SPacketSetSubtitleText packet
        ) {
            packet.subtitle = text;
            return packet;
        }
    }

    @Polymorphic(stringValue = "ACTIONBAR")
    @MessageVariant(maxVersion = Protocols.V1_16_5)
    public static class ActionBar extends SPacketTitle_1_16_5 {
        public CommonTypes.Text text;

        @Handler
        public static SPacketSetActionBarText handle(
                @Argument("text") CommonTypes.Text text,
                @DefaultConstruct SPacketSetActionBarText packet
        ) {
            packet.text = text;
            return packet;
        }
    }

    @Polymorphic(stringValue = "TIMES")
    @MessageVariant(maxVersion = Protocols.V1_16_5)
    public static class Times extends SPacketTitle_1_16_5 {
        @Type(Types.INT)
        public int fadeInTime;
        @Type(Types.INT)
        public int stayTime;
        @Type(Types.INT)
        public int fadeOutTime;

        @Handler
        public static SPacketSetTitlesAnimation handle(
                @Argument("fadeInTime") int fadeInTime,
                @Argument("stayTime") int stayTime,
                @Argument("fadeOutTime") int fadeOutTime,
                @DefaultConstruct SPacketSetTitlesAnimation packet
        ) {
            packet.fadeIn = fadeInTime;
            packet.stay = stayTime;
            packet.fadeOut = fadeOutTime;
            return packet;
        }
    }

    @Polymorphic(stringValue = {"CLEAR", "RESET"})
    @MessageVariant(maxVersion = Protocols.V1_16_5)
    public static class Clear extends SPacketTitle_1_16_5 {
        @Handler
        public static SPacketClearTitles handle(
                @Argument("mode") Mode mode,
                @DefaultConstruct SPacketClearTitles packet
        ) {
            packet.reset = mode == Mode.RESET;
            return packet;
        }
    }

    @NetworkEnum
    public enum Mode {
        TITLE, SUBTITLE, ACTIONBAR, TIMES, CLEAR, RESET
    }
}
