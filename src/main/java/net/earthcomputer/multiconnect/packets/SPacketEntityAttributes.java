package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.UUID;

@Message
public class SPacketEntityAttributes {
    public int entityId;
    public List<Property> properties;

    @Message
    public static class Property {
        public Identifier key;
        public double value;
        public List<Modifier> modifiers;

        @Message
        public static class Modifier {
            public UUID uuid;
            public double amount;
            @Type(Types.BYTE)
            public Operation operation;

            public enum Operation {
                ADDITION, MULTIPLY_BASE, MULTIPLY_TOTAL
            }
        }
    }
}
