package net.earthcomputer.multiconnect.datafix;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class DynamicRegistries1_17Fix extends OldExperimentalDynamicRegistriesFix {
    public DynamicRegistries1_17Fix(Schema outputSchema, boolean changesType) {
        super(outputSchema, changesType, "1.17");
    }

    @Override
    protected Dynamic<?> translateBiome(Dynamic<?> fromDynamic) {
        return fromDynamic;
    }
}
