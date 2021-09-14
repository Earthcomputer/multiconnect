package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;

import java.util.UUID;

@Message
public class SPacketBossBar {
    public UUID uuid;
    public Action action;

    @Polymorphic
    @Message
    public static abstract class Action {
        public int action;
    }

    @Polymorphic(intValue = 0)
    @Message
    public static class AddAction extends Action {
        public CommonTypes.Text title;
        public float health;
        public Color color;
        public Division division;
        @Type(Types.UNSIGNED_BYTE)
        public int flags;
    }

    @Polymorphic(intValue = 1)
    @Message
    public static class RemoveAction extends Action {}

    @Polymorphic(intValue = 2)
    @Message
    public static class UpdateHealthAction extends Action {
        public float health;
    }

    @Polymorphic(intValue = 3)
    @Message
    public static class UpdateTitleAction extends Action {
        public CommonTypes.Text title;
    }

    @Polymorphic(intValue = 4)
    @Message
    public static class UpdateStyle extends Action {
        public Color color;
        public Division division;
    }

    @Polymorphic(intValue = 5)
    @Message
    public static class UpdateFlags extends Action {
        @Type(Types.UNSIGNED_BYTE)
        public int flags;
    }

    public enum Color {
        PINK, BLUE, RED, GREEN, YELLOW, PURPLE, WHITE
    }

    public enum Division {
        NONE, SIX_NOTCHES, TEN_NOTCHES, TWELVE_NOTCHES, TWENTY_NOTCHES
    }
}
