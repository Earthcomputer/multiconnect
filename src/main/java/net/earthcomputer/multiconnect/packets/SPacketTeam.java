package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.NetworkEnum;
import net.earthcomputer.multiconnect.ap.Polymorphic;

import java.util.List;

@Message
public class SPacketTeam {
    public String name;
    public Action action;

    @Polymorphic
    @Message
    public static abstract class Action {
        public Mode mode;

        @NetworkEnum
        public enum Mode {
            CREATE, REMOVE, UPDATE_INFO, ADD_ENTITIES, REMOVE_ENTITIES
        }
    }

    @Polymorphic(stringValue = "CREATE")
    @Message
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
    @Message
    public static class RemoveAction extends Action {
    }

    @Polymorphic(stringValue = "UPDATE_INFO")
    @Message
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
    @Message
    public static class EntitiesAction extends Action {
        public List<String> entities;
    }
}
