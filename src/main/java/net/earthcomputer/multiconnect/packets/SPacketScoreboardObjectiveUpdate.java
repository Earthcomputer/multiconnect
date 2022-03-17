package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.NetworkEnum;
import net.earthcomputer.multiconnect.ap.Polymorphic;

@MessageVariant
public class SPacketScoreboardObjectiveUpdate {
    public String name;
    public Action action;

    @Polymorphic
    @MessageVariant
    public static abstract class Action {
        public Mode mode;

        @NetworkEnum
        public enum Mode {
            CREATE, REMOVE, UPDATE
        }
    }

    @Polymorphic(stringValue = {"CREATE", "UPDATE"})
    @MessageVariant
    public static class AdditiveAction extends Action {
        public CommonTypes.Text value;
        public ObjectiveType type;
    }

    @Polymorphic(stringValue = "REMOVE")
    @MessageVariant
    public static class RemoveAction extends Action {
    }

    @NetworkEnum
    public enum ObjectiveType {
        INTEGER, HEARTS
    }
}
