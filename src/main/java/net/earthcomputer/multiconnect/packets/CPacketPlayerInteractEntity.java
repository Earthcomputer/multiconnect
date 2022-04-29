package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.NetworkEnum;
import net.earthcomputer.multiconnect.ap.Polymorphic;

@MessageVariant
public class CPacketPlayerInteractEntity {
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