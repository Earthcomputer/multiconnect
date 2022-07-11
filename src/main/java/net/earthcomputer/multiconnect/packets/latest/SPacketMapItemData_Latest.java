package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.NetworkEnum;
import net.earthcomputer.multiconnect.ap.OnlyIf;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketMapItemData;

import java.util.List;
import java.util.Optional;

@MessageVariant(minVersion = Protocols.V1_17)
public class SPacketMapItemData_Latest implements SPacketMapItemData {
    public int mapId;
    public byte scale;
    public boolean locked;
    public Optional<List<SPacketMapItemData.Icon>> icons;
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

    @MessageVariant(minVersion = Protocols.V1_13)
    public static class Icon implements SPacketMapItemData.Icon {
        @Introduce(compute = "computeType")
        public SPacketMapItemData_Latest.Icon.Type type;
        public byte x;
        public byte z;
        @Introduce(compute = "computeDirection")
        public byte direction;
        @Introduce(defaultConstruct = true)
        public Optional<CommonTypes.Text> displayName;

        public static SPacketMapItemData_Latest.Icon.Type computeType(@Argument("metadata") byte metadata) {
            return SPacketMapItemData_Latest.Icon.Type.VALUES[(metadata >> 4) & 15];
        }

        public static byte computeDirection(@Argument("metadata") byte metadata) {
            return (byte) (metadata & 15);
        }

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
            ;

            public static final SPacketMapItemData_Latest.Icon.Type[] VALUES = values();
        }
    }
}
