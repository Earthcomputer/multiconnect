package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.NetworkEnum;
import net.earthcomputer.multiconnect.ap.OnlyIf;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;

import java.util.List;
import java.util.Optional;

@MessageVariant
public class SPacketMapUpdate {
    public int mapId;
    public byte scale;
    public boolean locked;
    public Optional<List<Icon>> icons;
    @Type(Types.UNSIGNED_BYTE)
    public int columns;
    @OnlyIf("hasColumns")
    public byte rows;
    @OnlyIf("hasColumns")
    public byte x;
    @OnlyIf("hasColumns")
    public byte z;
    @OnlyIf("hasColumns")
    public byte[] data;

    public static boolean hasColumns(@Argument("columns") int columns) {
        return columns > 0;
    }

    @MessageVariant
    public static class Icon {
        public Type type;
        public byte x;
        public byte z;
        public byte direction;
        public Optional<CommonTypes.Text> displayName;

        @NetworkEnum
        public enum Type {
            PLAYER,
            FRAME,
            RED_MARKER,
            BLUE_MARKER,
            TARGET_X,
            TARGET_POINT,
            PLAYER_OFF_MAP,
            PLAYER_OFF_LIMITS,
            MANSION,
            MONUMENT,
            BANNER_WHITE,
            BANNER_ORANGE,
            BANNER_MAGENTA,
            BANNER_LIGHT_BLUE,
            BANNER_YELLOW,
            BANNER_LIME,
            BANNER_PINK,
            BANNER_GRAY,
            BANNER_LIGHT_GRAY,
            BANNER_CYAN,
            BANNER_PURPLE,
            BANNER_BLUE,
            BANNER_BROWN,
            BANNER_GREEN,
            BANNER_RED,
            BANNER_BLACK,
            RED_X,
        }
    }
}
