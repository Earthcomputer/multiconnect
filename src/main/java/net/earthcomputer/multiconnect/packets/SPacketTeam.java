package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.NetworkEnum;
import net.earthcomputer.multiconnect.ap.Polymorphic;

import java.util.List;

@MessageVariant
public class SPacketTeam {
    public String name;
    public Action action;

    @Polymorphic
    @MessageVariant
    public static abstract class Action {
        public Mode mode;

        @NetworkEnum
        public enum Mode {
            CREATE, REMOVE, UPDATE_INFO, ADD_ENTITIES, REMOVE_ENTITIES
        }
    }

    @Polymorphic(stringValue = "CREATE")
    @MessageVariant
    public static class CreateAction extends Action {
        public CommonTypes.Text displayName;
        public byte friendlyFlags;
        public String nameTagVisibility;
        public String collisionRule;
        public CommonTypes.Formatting color;
        public CommonTypes.Text prefix;
        public CommonTypes.Text suffix;
        public List<String> entities;
    }

    @Polymorphic(stringValue = "REMOVE")
    @MessageVariant
    public static class RemoveAction extends Action {
    }

    @Polymorphic(stringValue = "UPDATE_INFO")
    @MessageVariant
    public static class UpdateInfoAction extends Action {
        public CommonTypes.Text displayName;
        public byte friendlyFlags;
        public String nameTagVisibility;
        public String collisionRule;
        public CommonTypes.Formatting color;
        public CommonTypes.Text prefix;
        public CommonTypes.Text suffix;
    }

    @Polymorphic(stringValue = {"ADD_ENTITIES", "REMOVE_ENTITIES"})
    @MessageVariant
    public static class EntitiesAction extends Action {
        public List<String> entities;
    }
}
