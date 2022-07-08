package net.earthcomputer.multiconnect.packets.v1_18_2;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.minecraft.util.Identifier;

@Polymorphic
@MessageVariant(minVersion = Protocols.V1_13, maxVersion = Protocols.V1_18_2)
public abstract class PositionSource_1_18_2 implements CommonTypes.PositionSource {
    @Registry(Registries.POSITION_SOURCE_TYPE)
    public Identifier type;

    @Polymorphic(stringValue = "block")
    @MessageVariant(minVersion = Protocols.V1_13, maxVersion = Protocols.V1_18_2)
    public static class Block extends PositionSource_1_18_2 implements CommonTypes.PositionSource.Block {
        public CommonTypes.BlockPos pos;
    }

    @Polymorphic(stringValue = "entity")
    @MessageVariant(minVersion = Protocols.V1_13, maxVersion = Protocols.V1_18_2)
    public static class Entity extends PositionSource_1_18_2 implements CommonTypes.PositionSource.Entity {
        public int entityId;
    }
}
