package net.earthcomputer.multiconnect.packets.v1_12_2;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketSetObjective;
import net.earthcomputer.multiconnect.packets.latest.SPacketSetObjective_Latest;

@Polymorphic
@MessageVariant(maxVersion = Protocols.V1_12_2)
public abstract class ScoreboardObjectiveUpdateAction_1_12_2 implements SPacketSetObjective.Action {
    public SPacketSetObjective_Latest.Action.Mode mode;

    @Polymorphic(stringValue = {"CREATE", "UPDATE"})
    @MessageVariant(maxVersion = Protocols.V1_12_2)
    public static class Additive extends ScoreboardObjectiveUpdateAction_1_12_2 implements SPacketSetObjective.AdditiveAction {
        public String value;
        public String type;
    }

    @Polymorphic(stringValue = "REMOVE")
    @MessageVariant(maxVersion = Protocols.V1_12_2)
    public static class Remove extends ScoreboardObjectiveUpdateAction_1_12_2 implements SPacketSetObjective.RemoveAction {
    }
}
