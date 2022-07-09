package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.NetworkEnum;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketUpdateAttributes;
import net.earthcomputer.multiconnect.protocols.v1_15.mixin.AttributesRenameAccessor;
import net.minecraft.resources.ResourceLocation;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@MessageVariant(minVersion = Protocols.V1_17)
public class SPacketUpdateAttributes_Latest implements SPacketUpdateAttributes {
    public int entityId;
    public List<SPacketUpdateAttributes.Property> properties;

    @MessageVariant(minVersion = Protocols.V1_16)
    public static class Property implements SPacketUpdateAttributes.Property {
        @Introduce(compute = "translateKey")
        public ResourceLocation key;
        public double value;
        public List<Modifier> modifiers;

        public static ResourceLocation translateKey(@Argument("key") String key) {
            String newKey = AttributesRenameAccessor.getRenames().getOrDefault(key, key).toLowerCase(Locale.ROOT);
            return new ResourceLocation(newKey);
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
