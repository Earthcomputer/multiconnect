package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.NetworkEnum;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;

import java.util.UUID;

@MessageVariant
public class SPacketBossBar {
    public UUID uuid;
    public Action action;

    @Polymorphic
    @MessageVariant
    public static abstract class Action {
        public int action;
    }

    @Polymorphic(intValue = 0)
    @MessageVariant
    public static class AddAction extends Action {
        public CommonTypes.Text title;
        public float health;
        public Color color;
        public Division division;
        @Type(Types.UNSIGNED_BYTE)
        public int flags;
    }

    @Polymorphic(intValue = 1)
    @MessageVariant
    public static class RemoveAction extends Action {}

    @Polymorphic(intValue = 2)
    @MessageVariant
    public static class UpdateHealthAction extends Action {
        public float health;
    }

    @Polymorphic(intValue = 3)
    @MessageVariant
    public static class UpdateTitleAction extends Action {
        public CommonTypes.Text title;
    }

    @Polymorphic(intValue = 4)
    @MessageVariant
    public static class UpdateStyle extends Action {
        public Color color;
        public Division division;
    }

    @Polymorphic(intValue = 5)
    @MessageVariant
    public static class UpdateFlags extends Action {
        @Type(Types.UNSIGNED_BYTE)
        public int flags;
    }

    @NetworkEnum
    public enum Color {
        PINK, BLUE, RED, GREEN, YELLOW, PURPLE, WHITE
    }

    @NetworkEnum
    public enum Division {
        NONE, SIX_NOTCHES, TEN_NOTCHES, TWELVE_NOTCHES, TWENTY_NOTCHES
    }
}
