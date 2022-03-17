package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;
import net.minecraft.util.Identifier;

@MessageVariant
public class SPacketVibration {
    public CommonTypes.BlockPos pos;
    public PositionSource positionSource;
    public int arrivalTicks;

    @Polymorphic
    @MessageVariant
    public static abstract class PositionSource {
        @Registry(Registries.POSITION_SOURCE_TYPE)
        public Identifier type;
    }

    @Polymorphic(stringValue = "block")
    @MessageVariant
    public static class BlockPositionSource extends PositionSource {
        public CommonTypes.BlockPos pos;
    }

    @Polymorphic(stringValue = "entity")
    @MessageVariant
    public static class EntityPositionSource extends PositionSource {
        public int entityId;
    }
}
