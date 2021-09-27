package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.NetworkEnum;
import net.earthcomputer.multiconnect.ap.Polymorphic;

@Message
public class SPacketScoreboardObjectiveUpdate {
    public String name;
    public Action action;

    @Polymorphic
    @Message
    public static abstract class Action {
        public Mode mode;

        @NetworkEnum
        public enum Mode {
            CREATE, REMOVE, UPDATE
        }
    }

    @Polymorphic(stringValue = {"CREATE", "UPDATE"})
    @Message
    public static class AdditiveAction extends Action {
        public CommonTypes.Text value;
        public ObjectiveType type;
    }

    @Polymorphic(stringValue = "REMOVE")
    @Message
    public static class RemoveAction extends Action {
    }

    @NetworkEnum
    public enum ObjectiveType {
        INTEGER, HEARTS
    }
}
