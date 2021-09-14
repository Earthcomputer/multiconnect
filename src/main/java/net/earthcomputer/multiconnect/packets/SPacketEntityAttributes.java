package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.Protocol;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.v1_16_5.SPacketEntityAttributes_1_16_5;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.UUID;

@Message(translateFromOlder = @Protocol(value = Protocols.V1_16_5, type = SPacketEntityAttributes_1_16_5.class))
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
