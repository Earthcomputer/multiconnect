package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.Polymorphic;

@Message
public class CPacketPlayerInteractEntity {
    public int entityId;
    public Action action;
    public boolean sneaking;

    @Polymorphic
    @Message
    public static abstract class Action {
        public Type type;

        public enum Type {
            INTERACT, ATTACK, INTERACT_AT
        }
    }

    @Polymorphic(stringValue = "INTERACT")
    @Message
    public static class InteractAction extends Action {
        public CommonTypes.Hand hand;
    }

    @Polymorphic(stringValue = "ATTACK")
    @Message
    public static class AttackAction extends Action {
    }

    @Polymorphic(stringValue = "INTERACT_AT")
    @Message
    public static class InteractAtAction extends Action {
        public float x;
        public float y;
        public float z;
        public CommonTypes.Hand hand;
    }
}
