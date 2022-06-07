package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.NetworkEnum;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketEntityAttributes;
import net.earthcomputer.multiconnect.protocols.v1_15_2.mixin.RenameItemStackAttributesFixAccessor;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@MessageVariant(minVersion = Protocols.V1_17)
public class SPacketEntityAttributes_Latest implements SPacketEntityAttributes {
    public int entityId;
    public List<SPacketEntityAttributes.Property> properties;

    @MessageVariant(minVersion = Protocols.V1_16)
    public static class Property implements SPacketEntityAttributes.Property {
        @Introduce(compute = "translateKey")
        public Identifier key;
        public double value;
        public List<Modifier> modifiers;

        public static Identifier translateKey(@Argument("key") String key) {
            String newKey = RenameItemStackAttributesFixAccessor.getRenames().getOrDefault(key, key).toLowerCase(Locale.ROOT);
            return new Identifier(newKey);
        }

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
