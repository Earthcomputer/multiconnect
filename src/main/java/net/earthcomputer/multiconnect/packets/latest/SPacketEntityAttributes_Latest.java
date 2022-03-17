package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.NetworkEnum;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketEntityAttributes;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.UUID;

@MessageVariant(minVersion = Protocols.V1_17)
public class SPacketEntityAttributes_Latest implements SPacketEntityAttributes {
    public int entityId;
    public List<Property> properties;

    @MessageVariant
    public static class Property {
        public Identifier key;
        public double value;
        public List<Modifier> modifiers;

        @MessageVariant
        public static class Modifier {
            public UUID uuid;
            public double amount;
            @Type(Types.BYTE)
            public Operation operation;

            @NetworkEnum
            public enum Operation {
                ADDITION, MULTIPLY_BASE, MULTIPLY_TOTAL
            }
        }
    }
}
