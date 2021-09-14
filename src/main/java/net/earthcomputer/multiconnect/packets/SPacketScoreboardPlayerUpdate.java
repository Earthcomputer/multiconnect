package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.Polymorphic;

@Message
public class SPacketScoreboardPlayerUpdate {
    public String entityName;
    public Action action;

    @Polymorphic
    @Message
    public static abstract class Action {
        public Mode mode;
        public String objectiveName;

        public enum Mode {
            UPDATE, REMOVE
        }
    }

    @Polymorphic(stringValue = "UPDATE")
    @Message
    public static class UpdateAction extends Action {
        public int value;
    }

    @Polymorphic(stringValue = "REMOVE")
    @Message
    public static class RemoveAction extends Action {
    }
}
