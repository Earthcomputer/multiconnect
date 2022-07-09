package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.NetworkEnum;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketTeam;
import net.earthcomputer.multiconnect.packets.v1_18_2.Text_1_18_2;

import java.util.List;

@MessageVariant
public class SPacketTeam_Latest implements SPacketTeam {
    public String name;
    public SPacketTeam.Action action;

    @Polymorphic
    @MessageVariant(minVersion = Protocols.V1_13)
    public static abstract class Action implements SPacketTeam.Action {
        public Mode mode;

        @NetworkEnum
        public enum Mode {
            CREATE, REMOVE, UPDATE_INFO, ADD_ENTITIES, REMOVE_ENTITIES
        }
    }

    @Polymorphic(stringValue = "CREATE")
    @MessageVariant(minVersion = Protocols.V1_13)
    public static class CreateAction extends net.earthcomputer.multiconnect.packets.latest.SPacketTeam_Latest.Action implements SPacketTeam.CreateAction {
        @Introduce(compute = "computeDisplayName")
        public CommonTypes.Text displayName;
        public byte friendlyFlags;
        public String nameTagVisibility;
        public String collisionRule;
        @Introduce(compute = "computeColor")
        public CommonTypes.Formatting color;
        @Introduce(compute = "computePrefix")
        public CommonTypes.Text prefix;
        @Introduce(compute = "computeSuffix")
        public CommonTypes.Text suffix;
        public List<String> entities;

        public static CommonTypes.Text computeDisplayName(@Argument("displayName") String displayName) {
            return Text_1_18_2.createLiteral(displayName);
        }

        public static CommonTypes.Text computePrefix(@Argument("prefix") String prefix) {
            return Text_1_18_2.createLiteral(prefix);
        }

        public static CommonTypes.Text computeSuffix(@Argument("suffix") String suffix) {
            return Text_1_18_2.createLiteral(suffix);
        }

        public static CommonTypes.Formatting computeColor(@Argument("color") byte color) {
            if (color < 0 || color >= 16) {
                return CommonTypes.Formatting.RESET;
            }
            return CommonTypes.Formatting.VALUES[color];
        }
    }

    @Polymorphic(stringValue = "REMOVE")
    @MessageVariant(minVersion = Protocols.V1_13)
    public static class RemoveAction extends net.earthcomputer.multiconnect.packets.latest.SPacketTeam_Latest.Action implements SPacketTeam.RemoveAction {
    }

    @Polymorphic(stringValue = "UPDATE_INFO")
    @MessageVariant(minVersion = Protocols.V1_13)
    public static class UpdateInfoAction extends net.earthcomputer.multiconnect.packets.latest.SPacketTeam_Latest.Action implements SPacketTeam.UpdateInfoAction {
        @Introduce(compute = "computeDisplayName")
        public CommonTypes.Text displayName;
        public byte friendlyFlags;
        public String nameTagVisibility;
        public String collisionRule;
        @Introduce(compute = "computeColor")
        public CommonTypes.Formatting color;
        @Introduce(compute = "computePrefix")
        public CommonTypes.Text prefix;
        @Introduce(compute = "computeSuffix")
        public CommonTypes.Text suffix;

        public static CommonTypes.Text computeDisplayName(@Argument("displayName") String displayName) {
            return Text_1_18_2.createLiteral(displayName);
        }

        public static CommonTypes.Text computePrefix(@Argument("prefix") String prefix) {
            return Text_1_18_2.createLiteral(prefix);
        }

        public static CommonTypes.Text computeSuffix(@Argument("suffix") String suffix) {
            return Text_1_18_2.createLiteral(suffix);
        }

        public static CommonTypes.Formatting computeColor(@Argument("color") byte color) {
            if (color < 0 || color >= 16) {
                return CommonTypes.Formatting.RESET;
            }
            return CommonTypes.Formatting.VALUES[color];
        }
    }

    @Polymorphic(stringValue = {"ADD_ENTITIES", "REMOVE_ENTITIES"})
    @MessageVariant(minVersion = Protocols.V1_13)
    public static class EntitiesAction extends net.earthcomputer.multiconnect.packets.latest.SPacketTeam_Latest.Action implements SPacketTeam.EntitiesAction {
        public List<String> entities;
    }
}
