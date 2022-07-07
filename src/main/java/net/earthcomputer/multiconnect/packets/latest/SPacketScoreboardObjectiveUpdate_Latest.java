package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.NetworkEnum;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketScoreboardObjectiveUpdate;
import net.earthcomputer.multiconnect.packets.v1_18_2.Text_1_18_2;

@MessageVariant
public class SPacketScoreboardObjectiveUpdate_Latest implements SPacketScoreboardObjectiveUpdate {
    public String name;
    public SPacketScoreboardObjectiveUpdate.Action action;

    @Polymorphic
    @MessageVariant(minVersion = Protocols.V1_13)
    public static abstract class Action implements SPacketScoreboardObjectiveUpdate.Action {
        public Mode mode;

        @NetworkEnum
        public enum Mode {
            CREATE, REMOVE, UPDATE
        }
    }

    @Polymorphic(stringValue = {"CREATE", "UPDATE"})
    @MessageVariant(minVersion = Protocols.V1_13)
    public static class AdditiveAction extends Action implements SPacketScoreboardObjectiveUpdate.AdditiveAction {
        @Introduce(compute = "computeValue")
        public CommonTypes.Text value;
        @Introduce(compute = "computeType")
        public ObjectiveType type;

        public static CommonTypes.Text computeValue(@Argument("value") String value) {
            return Text_1_18_2.createLiteral(value);
        }

        public static ObjectiveType computeType(@Argument("type") String type) {
            return "hearts".equals(type) ? ObjectiveType.HEARTS : ObjectiveType.INTEGER;
        }
    }

    @Polymorphic(stringValue = "REMOVE")
    @MessageVariant(minVersion = Protocols.V1_13)
    public static class RemoveAction extends Action implements SPacketScoreboardObjectiveUpdate.RemoveAction {
    }

    @NetworkEnum
    public enum ObjectiveType {
        INTEGER, HEARTS
    }
}
