package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;
import net.minecraft.util.Identifier;

@Message
public class SPacketVibration {
    public CommonTypes.BlockPos pos;
    public PositionSource positionSource;
    public int arrivalTicks;

    @Polymorphic
    @Message
    public static abstract class PositionSource {
        @Registry(Registries.POSITION_SOURCE_TYPE)
        public Identifier type;
    }

    @Polymorphic(stringValue = "block")
    @Message
    public static class BlockPositionSource extends PositionSource {
        public CommonTypes.BlockPos pos;
    }

    @Polymorphic(stringValue = "entity")
    @Message
    public static class EntityPositionSource extends PositionSource {
        public int entityId;
    }
}
