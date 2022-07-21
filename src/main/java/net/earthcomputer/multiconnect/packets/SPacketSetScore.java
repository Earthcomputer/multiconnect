package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.NetworkEnum;
import net.earthcomputer.multiconnect.ap.Polymorphic;

@MessageVariant
public class SPacketSetScore {
    public String entityName;
    public Action action;

    @Polymorphic
    @MessageVariant
    public static abstract class Action {
        public Mode mode;
        public String objectiveName;

        @NetworkEnum
        public enum Mode {
            UPDATE, REMOVE
        }
    }

    @Polymorphic(stringValue = "UPDATE")
    @MessageVariant
    public static class UpdateAction extends Action {
        public int value;
    }

    @Polymorphic(stringValue = "REMOVE")
    @MessageVariant
    public static class RemoveAction extends Action {
    }
}
