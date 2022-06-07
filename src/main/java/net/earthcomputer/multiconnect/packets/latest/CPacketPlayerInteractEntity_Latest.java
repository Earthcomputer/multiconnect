package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.NetworkEnum;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CPacketPlayerInteractEntity;
import net.earthcomputer.multiconnect.packets.CommonTypes;

@MessageVariant(minVersion = Protocols.V1_16)
public class CPacketPlayerInteractEntity_Latest implements CPacketPlayerInteractEntity {
    public int entityId;
    public Action action;
    public boolean sneaking;

    @Polymorphic
    @MessageVariant
    public static abstract class Action {
        public Type type;

        @NetworkEnum
        public enum Type {
            INTERACT, ATTACK, INTERACT_AT
        }
    }

    @Polymorphic(stringValue = "INTERACT")
    @MessageVariant
    public static class InteractAction extends Action {
        public CommonTypes.Hand hand;
    }

    @Polymorphic(stringValue = "ATTACK")
    @MessageVariant
    public static class AttackAction extends Action {
    }

    @Polymorphic(stringValue = "INTERACT_AT")
    @MessageVariant
    public static class InteractAtAction extends Action {
        public float x;
        public float y;
        public float z;
        public CommonTypes.Hand hand;
    }
}
