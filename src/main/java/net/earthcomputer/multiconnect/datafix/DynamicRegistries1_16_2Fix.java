package net.earthcomputer.multiconnect.datafix;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class DynamicRegistries1_16_2Fix extends OldExperimentalDynamicRegistriesFix {
    public DynamicRegistries1_16_2Fix(Schema outputSchema, boolean changesType) {
        super(outputSchema, changesType, "1.16.2");
    }

    @Override
    protected Dynamic<?> translateBiome(Dynamic<?> fromDynamic) {
        return fromDynamic;
    }
}
