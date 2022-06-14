package net.earthcomputer.multiconnect.packets.v1_12_2;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;

@Polymorphic
@MessageVariant(maxVersion = Protocols.V1_12_2)
public abstract class Particle_1_12_2 implements CommonTypes.Particle {
    @Registry(Registries.PARTICLE_TYPE)
    @Type(Types.INT)
    public int particleId;

    @Polymorphic(stringValue = {"block", "falling_dust", "multiconnect:block_dust"})
    @MessageVariant(maxVersion = Protocols.V1_12_2)
    public static class BlockState extends Particle_1_12_2 implements CommonTypes.Particle.BlockState {
        public int blockStateId;
    }

    @Polymorphic(stringValue = "item")
    @MessageVariant(maxVersion = Protocols.V1_12_2)
    public static class Item extends Particle_1_12_2 implements CommonTypes.Particle.Item {
        public int itemId;
        public int damage;
    }

    @Polymorphic(stringValue = "dust")
    @MessageVariant(maxVersion = Protocols.V1_12_2)
    public static class Dust extends Particle_1_12_2 implements CommonTypes.Particle.Dust {
    }

    @Polymorphic(otherwise = true)
    @MessageVariant(maxVersion = Protocols.V1_12_2)
    public static class Simple extends Particle_1_12_2 implements CommonTypes.Particle.Simple {
    }
}
