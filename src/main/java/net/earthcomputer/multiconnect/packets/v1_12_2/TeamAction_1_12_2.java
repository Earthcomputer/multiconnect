package net.earthcomputer.multiconnect.packets.v1_12_2;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketTeam;
import net.earthcomputer.multiconnect.packets.latest.SPacketTeam_Latest;

import java.util.List;

@Polymorphic
@MessageVariant(maxVersion = Protocols.V1_12_2)
public abstract class TeamAction_1_12_2 implements SPacketTeam.Action {
    public SPacketTeam_Latest.Action.Mode mode;

    @Polymorphic(stringValue = "CREATE")
    @MessageVariant(maxVersion = Protocols.V1_12_2)
    public static class CreateAction extends TeamAction_1_12_2 implements SPacketTeam.CreateAction {
        public String displayName;
        public String prefix;
        public String suffix;
        public byte friendlyFlags;
        public String nameTagVisibility;
        public String collisionRule;
        public byte color;
        public List<String> entities;
    }

    @Polymorphic(stringValue = "REMOVE")
    @MessageVariant(maxVersion = Protocols.V1_12_2)
    public static class RemoveAction extends TeamAction_1_12_2 implements SPacketTeam.RemoveAction {
    }

    @Polymorphic(stringValue = "UPDATE_INFO")
    @MessageVariant(maxVersion = Protocols.V1_12_2)
    public static class UpdateInfoAction extends TeamAction_1_12_2 implements SPacketTeam.UpdateInfoAction {
        public String displayName;
        public String prefix;
        public String suffix;
        public byte friendlyFlags;
        public String nameTagVisibility;
        public String collisionRule;
        public byte color;
    }

    @Polymorphic(stringValue = {"ADD_ENTITIES", "REMOVE_ENTITIES"})
    @MessageVariant(maxVersion = Protocols.V1_12_2)
    public static class EntitiesAction extends TeamAction_1_12_2 implements SPacketTeam.EntitiesAction {
        public List<String> entities;
    }
}
